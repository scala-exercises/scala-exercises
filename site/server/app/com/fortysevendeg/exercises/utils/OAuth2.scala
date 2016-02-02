package com.fortysevendeg.exercises.utils

import com.fortysevendeg.exercises.services.UserServices
import play.api.{ Application, Play }
import play.api.http.{ HeaderNames, MimeTypes }
import play.api.mvc.{ Action, Controller, Results }
import play.api.libs.ws._
import play.api.libs.ws.ning.NingWSClient
import scala.concurrent.ExecutionContext.Implicits.global
import com.fortysevendeg.exercises.services._

import cats.data.Xor
import scala.concurrent.Future

object OAuth2 {

  implicit def application: Application = Play.current

  lazy val githubAuthId = application.configuration.getString("github.client.id").get
  lazy val githubAuthSecret = application.configuration.getString("github.client.secret").get

  def getAuthorizationUrl(redirectUri: String, scope: String, state: String): String = {
    val baseUrl = application.configuration.getString("github.redirect.url").get
    baseUrl.format(githubAuthId, redirectUri, scope, state)
  }
}

class OAuth2Controller(implicit userService: UserServices) extends Controller {

  import OAuth2._

  val ws = NingWSClient()

  def getToken(code: String): Future[String] = {
    val tokenResponse = ws.url("https://github.com/login/oauth/access_token")
      .withQueryString(
        "client_id" → githubAuthId,
        "client_secret" → githubAuthSecret,
        "code" → code
      ).
        withHeaders(HeaderNames.ACCEPT → MimeTypes.JSON).
        post(Results.EmptyContent())

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
          Redirect(com.fortysevendeg.exercises.utils.routes.OAuth2Controller.success()).withSession("oauth-token" → accessToken)
        }.recover {
          case ex: IllegalStateException ⇒ Unauthorized(ex.getMessage)
        }
      } else {
        Future.successful(BadRequest("Invalid github login"))
      }
    }).getOrElse(Future.successful(BadRequest("No parameters supplied")))
  }

  def success() = Action.async { request ⇒
    request.session.get("oauth-token").fold(Future.successful(Unauthorized("No way Jose"))) { authToken ⇒
      ws.url("https://api.github.com/user").
        withHeaders(HeaderNames.AUTHORIZATION → s"token $authToken").
        get().map { response ⇒

          val login = (response.json \ "login").as[String]
          val name = (response.json \ "name").as[String]
          val githubId = (response.json \ "id").as[Long]
          val avatarUrl = (response.json \ "avatar_url").as[String]
          val htmlUrl = (response.json \ "html_url").as[String]
          val email = (response.json \ "email").as[String]

          val result = userService.getUserOrCreate(
            login,
            name,
            githubId.toString,
            avatarUrl,
            htmlUrl,
            email
          )
          result match {
            case Xor.Right(_) ⇒ Redirect("/").withSession("oauth-token" → authToken, "user" → login)
            case Xor.Left(_)  ⇒ InternalServerError("Failed to save user information")
          }

        }
    }
  }

  def logout() = Action(Redirect("/").withNewSession)
}
