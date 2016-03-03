/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises.utils

import cats.data.Xor
import cats.std.option._
import cats.syntax.cartesian._
import com.fortysevendeg.exercises.app._
import com.fortysevendeg.exercises.persistence.domain.UserCreation
import com.fortysevendeg.exercises.services.free.UserOps
import com.fortysevendeg.exercises.services.interpreters.ProdInterpreters
import com.fortysevendeg.exercises.services.interpreters.FreeExtensions._
import doobie.imports._
import play.api.http.{ HeaderNames, MimeTypes }
import play.api.libs.ws._
import play.api.mvc.{ Action, Controller, Results, BodyParsers }
import play.api.{ Application, Play }
import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scalaz.concurrent.Task

object OAuth2 {

  implicit def application: Application = Play.current

  lazy val githubAuthId = application.configuration.getString("github.client.id").get
  lazy val githubAuthSecret = application.configuration.getString("github.client.secret").get

  def getAuthorizationUrl(redirectUri: String, scope: String, state: String): String = {
    val baseUrl = application.configuration.getString("github.redirect.url").get
    baseUrl.format(githubAuthId, redirectUri, scope, state)
  }
}

class OAuth2Controller(
    implicit
    userOps: UserOps[ExercisesApp],
    T:       Transactor[Task],
    ws:      WSClient
) extends Controller with ProdInterpreters {

  import OAuth2._

  def githubTokenRequest(githubClientToken: String, githubSecretToken: String, code: String) = {
    ws.url("https://github.com/login/oauth/access_token")
      .withQueryString(
        "client_id" → githubClientToken,
        "client_secret" → githubSecretToken,
        "code" → code
      )
      .withHeaders(HeaderNames.ACCEPT → MimeTypes.JSON)
      .post(Results.EmptyContent())
  }

  def fetchGitHubToken(githubClientToken: String, githubSecretToken: String, code: String): Future[Option[String]] = for {
    response ← githubTokenRequest(githubClientToken, githubSecretToken, code)
    theToken ← Future.successful((response.json \ "access_token").asOpt[String])
  } yield theToken

  case class GitHubUser(
    login:     String,
    name:      Option[String],
    githubId:  Long,
    avatarUrl: String,
    htmlUrl:   String,
    email:     Option[String]
  )

  implicit val readGithubUser: Reads[GitHubUser] = (
    (JsPath \ "login").read[String] and
    (JsPath \ "name").readNullable[String] and
    (JsPath \ "id").read[Long] and
    (JsPath \ "avatar_url").read[String] and
    (JsPath \ "html_url").read[String] and
    (JsPath \ "email").readNullable[String]
  )(GitHubUser.apply _)

  def githubUserRequest(authToken: String) =
    ws.url("https://api.github.com/user").withHeaders(HeaderNames.AUTHORIZATION → s"token $authToken").get()

  def fetchGitHubUser(authToken: String): Future[Option[GitHubUser]] = for {
    response ← githubUserRequest(authToken)
  } yield response.json.validate[GitHubUser] match {
    case ok: JsSuccess[GitHubUser] ⇒ Some(ok.get)
    case _                         ⇒ None
  }

  def getToken(code: String): Future[String] = for {
    maybeToken ← fetchGitHubToken(githubAuthId, githubAuthSecret, code)
    err = Future.failed[String](new IllegalStateException("Unable to retrieve GitHub token."))
    response ← maybeToken.fold(err)(Future.successful(_))
  } yield response

  def callback(codeOpt: Option[String] = None, stateOpt: Option[String] = None) = Action.async { implicit request ⇒
    lazy val missingParams = Future.successful(BadRequest("Missing query parameters: make sure `code` and `state` are present."))

    val params: Option[(String, String, String)] = (codeOpt |@| stateOpt |@| request.session.get("oauth-state")).tupled

    params.fold(missingParams)(params ⇒ {
      val (code, state, oauthState) = params
      if (state == oauthState) {
        (for {
          accessToken ← getToken(code)
          successURL = com.fortysevendeg.exercises.utils.routes.OAuth2Controller.success()
        } yield Redirect(successURL).withSession("oauth-token" → accessToken)).recover {
          case ex: IllegalStateException ⇒ Unauthorized(ex.getMessage)
        }
      } else {
        Future.successful(BadRequest("Invalid github login"))
      }
    })
  }

  def success() = Action.async { request ⇒
    lazy val unauthorized = Future.successful(Unauthorized("Missing OAuth token"))
    request.session.get("oauth-token").fold(unauthorized) { authToken ⇒
      for {
        maybeGhUser ← fetchGitHubUser(authToken)
        response = maybeGhUser.fold(InternalServerError("Failed to fetch GitHub profile"))(ghUser ⇒
          userOps.getOrCreate(
            UserCreation.Request(
              ghUser.login,
              ghUser.name,
              ghUser.githubId.toString,
              ghUser.avatarUrl,
              ghUser.htmlUrl,
              ghUser.email
            )
          ).runTask match {
              case Xor.Right(_) ⇒ Redirect(
                request.headers.get("referer") match {
                  case Some(url) if !url.contains("github") ⇒ url
                  case _                                    ⇒ "/"
                }
              )
              case Xor.Left(_) ⇒ InternalServerError("Failed to save user information")
            })
      } yield response
    }
  }

  def logout() = Action(Redirect("/").withNewSession)
}
