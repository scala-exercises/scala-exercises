/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises.utils

import cats.data.Xor
import com.fortysevendeg.exercises.app._
import com.fortysevendeg.exercises.persistence.domain.UserCreation
import com.fortysevendeg.exercises.services.free.UserOps
import com.fortysevendeg.exercises.services.interpreters.ProdInterpreters
import com.fortysevendeg.exercises.services.interpreters.FreeExtensions._
import doobie.imports._
import play.api.http.{ HeaderNames, MimeTypes }
import play.api.libs.ws._
import play.api.mvc.{ Action, Controller, Results }
import play.api.{ Application, Play }

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

  def getToken(code: String): Future[String] = {
    // TODO: extract request to fn
    val tokenResponse = ws.url("https://github.com/login/oauth/access_token")
      .withQueryString(
        "client_id" → githubAuthId,
        "client_secret" → githubAuthSecret,
        "code" → code
      ).
        withHeaders(HeaderNames.ACCEPT → MimeTypes.JSON).
        post(Results.EmptyContent())

    // TODO: for-comprehension
    // TODO: Reads[] for token response payload
    tokenResponse.flatMap { response ⇒
      (response.json \ "access_token").asOpt[String].fold(Future.failed[String](new IllegalStateException("Sod off!"))) { accessToken ⇒
        Future.successful(accessToken)
      }
    }
  }

  def callback(codeOpt: Option[String] = None, stateOpt: Option[String] = None) = Action.async { implicit request ⇒
    (for {
      code ← codeOpt
      state ← stateOpt
      oauthState ← request.session.get("oauth-state")
    } yield {

      if (state == oauthState) {
        getToken(code).map { accessToken ⇒
          val successURL = com.fortysevendeg.exercises.utils.routes.OAuth2Controller.success()
          Redirect(successURL).withSession("oauth-token" → accessToken)
        }.recover {
          case ex: IllegalStateException ⇒ Unauthorized(ex.getMessage)
        }
      } else {
        Future.successful(BadRequest("Invalid github login"))
      }
    }).getOrElse(Future.successful(BadRequest("No parameters supplied")))
  }

  def success() = Action.async { request ⇒
    // xxx: option
    request.session.get("oauth-token").fold(Future.successful(Unauthorized("Unauthorized"))) { authToken ⇒
      // xxx: future
      ws.url("https://api.github.com/user").
        withHeaders(HeaderNames.AUTHORIZATION → s"token $authToken").
        get().map { response ⇒
          // TODO: refactor to Reads
          val login = (response.json \ "login").as[String]
          val name = (response.json \ "name").asOpt[String]
          val githubId = (response.json \ "id").as[Long]
          val avatarUrl = (response.json \ "avatar_url").as[String]
          val htmlUrl = (response.json \ "html_url").as[String]
          val email = (response.json \ "email").asOpt[String]

          // TODO: user user ops instead of repository + transactor
          val ops = for {
            user ← userOps.getOrCreate(UserCreation.Request(
              login,
              name,
              githubId.toString,
              avatarUrl,
              htmlUrl,
              email
            ))
          } yield user

          ops.runTask match {
            case Xor.Right(_) ⇒ Redirect(request.headers.get("referer") match {
              case Some(url) if !url.contains("github") ⇒ url
              case _                                    ⇒ "/"
            }).withSession("oauth-token" → authToken, "user" → login)
            case Xor.Left(_) ⇒ InternalServerError("Failed to save user information")
          }

        }
    }
  }

  def logout() = Action(Redirect("/").withNewSession)
}
