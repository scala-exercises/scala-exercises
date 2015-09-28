package controllers

import java.util.UUID

import utils.OAuth2
import play.api._
import play.api.mvc._
import shared.SharedMessages

object Application extends Controller {

  def index = Action { implicit request =>

    val oauth2 = new OAuth2(Play.current)
    val callbackUrl = utils.routes.OAuth2.callback(None, None).absoluteURL()
    val logoutUrl = utils.routes.OAuth2.logout().absoluteURL()
    val scope = "user"
    val state = UUID.randomUUID().toString
    val redirectUrl = oauth2.getAuthorizationUrl(callbackUrl, scope, state)

    request.session.get("oauth-token").map { token =>
      Ok(views.html.index(SharedMessages.itWorks, logoutUrl))
    }.getOrElse {
      Ok(views.html.index("Not logged", redirectUrl)).withSession("oauth-state" -> state)
    }
  }

  def home = Action { implicit request =>

    Ok(views.html.templates.home())

  }

}
