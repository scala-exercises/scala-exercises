package controllers

import java.util.UUID

import services.UserServiceImpl
import services.messages.GetUserByLoginRequest
import services.parser.ExercisesService
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

    val sections = ExercisesService.sections

    request.session.get("oauth-token").map { token ⇒
      val userService = new UserServiceImpl
      val user = userService.getUserByLogin(GetUserByLoginRequest(login = request.session.get("user").getOrElse("")))
      user.map(response ⇒ Ok(views.html.templates.home(user = response.user, sections = sections)))
    }.getOrElse {
      Future.successful(Ok(views.html.templates.home(user = None, sections = sections, redirectUrl = Option(redirectUrl))).withSession("oauth-state" -> state))
    }

  }

  def section(sec: String) = Action.async { implicit request ⇒
    ExercisesService.sections.find(s ⇒ s.title == sec) match {
      case Some(s) ⇒ Future(Redirect(s"$sec/${s.categories.head}"))
      case _       ⇒ Future(Ok("Section not found"))
    }
  }

  def category(sec: String, cat: String) = Action.async { implicit request ⇒
    val section = ExercisesService.sections.find(s ⇒ s.title == sec)
    val category = ExercisesService.category(sec, cat).headOption

    (section, category) match {
      case (Some(s), Some(c)) ⇒ Future(Ok(views.html.templates.exercises(s, c)))
      case (Some(s), None)    ⇒ Future(Redirect(s.categories.head))
      case _                  ⇒ Future(Ok("Category not found"))
    }
  }

  def javascriptRoutes = Action { implicit request ⇒
    Ok(
      JavaScriptReverseRouter("jsRoutes")(
        routes.javascript.ExercisesController.sections,
        routes.javascript.ExercisesController.category,
        routes.javascript.ExercisesController.evaluate
      )
    ).as("text/javascript")
  }

}

