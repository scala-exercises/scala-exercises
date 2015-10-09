package controllers

import java.util.UUID

import services.UserServiceImpl
import services.messages.GetUserByLoginRequest
import utils.OAuth2
import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

object Application extends Controller {

  def index = Action.async { implicit request =>

    val oauth2 = new OAuth2(Play.current)
    val callbackUrl = utils.routes.OAuth2.callback(None, None).absoluteURL()
    val logoutUrl = utils.routes.OAuth2.logout().absoluteURL()
    val scope = "user"
    val state = UUID.randomUUID().toString
    val redirectUrl = oauth2.getAuthorizationUrl(callbackUrl, scope, state)

    request.session.get("oauth-token").map { token =>
      val userService = new UserServiceImpl
      val user = userService.getUserByLogin(GetUserByLoginRequest(login = request.session.get("user").getOrElse("")))
      user.map(response => Ok(views.html.templates.home(user = response.user)))
    }.getOrElse {
      Future.successful(Ok(views.html.templates.home(user = None, redirectUrl = Option(redirectUrl))).withSession("oauth-state" -> state))
    }

  }


}

