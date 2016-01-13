package utils

import play.api.{ Application, Play }
import play.api.http.{ HeaderNames, MimeTypes }
import play.api.mvc.{ Action, Controller, Results }
import play.api.libs.ws.WS
import services.messages.GetUserOrCreateRequest
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future
import services.UserServiceImpl

class OAuth2(application: Application) {
  lazy val githubAuthId = application.configuration.getString("github.client.id").get
  lazy val githubAuthSecret = application.configuration.getString("github.client.secret").get

  def getAuthorizationUrl(redirectUri: String, scope: String, state: String): String = {
    val baseUrl = application.configuration.getString("github.redirect.url").get
    baseUrl.format(githubAuthId, redirectUri, scope, state)
  }

  def getToken(code: String): Future[String] = {
    val tokenResponse = WS.url("https://github.com/login/oauth/access_token")(application).
      withQueryString(
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
}

object OAuth2 extends Controller {
  lazy val oauth2 = new OAuth2(Play.current)

  def callback(codeOpt: Option[String] = None, stateOpt: Option[String] = None) = Action.async { implicit request ⇒
    (for {
      code ← codeOpt
      state ← stateOpt
      oauthState ← request.session.get("oauth-state")
    } yield {

      if (state == oauthState) {
        oauth2.getToken(code).map { accessToken ⇒
          Redirect(utils.routes.OAuth2.success()).withSession("oauth-token" → accessToken)
        }.recover {
          case ex: IllegalStateException ⇒ Unauthorized(ex.getMessage)
        }
      } else {
        Future.successful(BadRequest("Invalid github login"))
      }
    }).getOrElse(Future.successful(BadRequest("No parameters supplied")))
  }

  def success() = Action.async { request ⇒
    implicit val app = Play.current
    request.session.get("oauth-token").fold(Future.successful(Unauthorized("No way Jose"))) { authToken ⇒
      WS.url("https://api.github.com/user").
        withHeaders(HeaderNames.AUTHORIZATION → s"token $authToken").
        get().map { response ⇒

          val login = (response.json \ "login").as[String]
          val name = (response.json \ "name").as[String]
          val github_id = (response.json \ "id").as[Long]
          val avatar_url = (response.json \ "avatar_url").as[String]
          val html_url = (response.json \ "html_url").as[String]
          val email = (response.json \ "email").as[String]

          val userService = new UserServiceImpl

          val result = for {
            res ← userService.getUserOrCreate(GetUserOrCreateRequest(
              login = login,
              name = name,
              github_id = github_id.toString,
              picture_url = avatar_url,
              github_url = html_url,
              email = email
            ))
          } yield res.user

          Redirect("/").withSession("oauth-token" → authToken, "user" → login)

        }
    }
  }

  def logout() = Action(Redirect("/").withNewSession)
}