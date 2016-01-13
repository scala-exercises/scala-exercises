package controllers

import java.util.UUID

import services.UserServiceImpl
import services.messages.GetUserByLoginRequest
import services.ExercisesService
import utils.OAuth2
import play.api._
import play.api.routing.JavaScriptReverseRouter
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

object Application extends Controller {

  def index = Action.async { implicit request ⇒

    val oauth2 = new OAuth2(Play.current)
    val callbackUrl = utils.routes.OAuth2.callback(None, None).absoluteURL()
    val logoutUrl = utils.routes.OAuth2.logout().absoluteURL()
    val scope = "user"
    val state = UUID.randomUUID().toString
    val redirectUrl = oauth2.getAuthorizationUrl(callbackUrl, scope, state)

    val libraries = ExercisesService.libraries

    request.session.get("oauth-token").map { token ⇒
      val userService = new UserServiceImpl
      val user = userService.getUserByLogin(GetUserByLoginRequest(login = request.session.get("user").getOrElse("")))
      user.map(response ⇒ Ok(views.html.templates.home.index(user = response.user, libraries = libraries)))
    }.getOrElse {
      Future.successful(Ok(views.html.templates.home.index(user = None, libraries = libraries, redirectUrl = Option(redirectUrl))).withSession("oauth-state" → state))
    }

  }

  def library(libraryName: String) = Action.async { implicit request ⇒
    ExercisesService.libraries.find(_.name == libraryName) match {
      case Some(library) ⇒ Future(Redirect(s"$libraryName/${library.sectionNames.head}"))
      case _             ⇒ Future(Ok("Library not found"))
    }
  }

  def section(libraryName: String, sectionName: String) = Action.async { implicit request ⇒

    val library = ExercisesService.libraries.find(_.name == libraryName)
    val section = ExercisesService.section(libraryName, sectionName).headOption

    (library, section) match {
      case (Some(l), Some(s)) ⇒ Future(Ok(views.html.templates.library.index(l, s)))
      case (Some(l), None)    ⇒ Future(Redirect(l.sectionNames.head))
      case _                  ⇒ Future(Ok("Section not found"))
    }
  }

  def javascriptRoutes = Action { implicit request ⇒
    Ok(
      JavaScriptReverseRouter("jsRoutes")(
        routes.javascript.ExercisesController.libraries,
        routes.javascript.ExercisesController.section,
        routes.javascript.ExercisesController.evaluate
      )
    ).as("text/javascript")
  }

}
