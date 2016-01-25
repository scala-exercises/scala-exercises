package com.fortysevendeg.exercises.controllers

import cats.Monad
import cats.data.NonEmptyList
import cats.data.Xor
import cats.free.Free
import com.fortysevendeg.exercises.services.UserServices
import com.fortysevendeg.shared.free.ExerciseOps
import java.util.UUID

import com.fortysevendeg.exercises.services.UserServiceImpl
import com.fortysevendeg.exercises.services.messages.GetUserByLoginRequest
import com.fortysevendeg.exercises.services.ExercisesService
import com.fortysevendeg.exercises.utils.OAuth2
import play.api._
import play.api.routing.JavaScriptReverseRouter
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import com.fortysevendeg.exercises.services._
import com.fortysevendeg.exercises.app._
import scalaz.concurrent.Task
import scalaz.{ \/-, -\/ }

import scala.concurrent.Future

class ApplicationController(userService: UserServices)(implicit exerciseOps: ExerciseOps[ExercisesApp]) extends Controller {

  def index = Action.async { implicit request ⇒

    val scope = "user"
    val state = UUID.randomUUID().toString
    val callbackUrl = com.fortysevendeg.exercises.utils.routes.OAuth2Controller.callback(None, None).absoluteURL()
    val logoutUrl = com.fortysevendeg.exercises.utils.routes.OAuth2Controller.logout().absoluteURL()
    val redirectUrl = OAuth2.getAuthorizationUrl(callbackUrl, scope, state)
    exerciseOps.getLibraries.runTask match {
      case \/-(libraries) ⇒
        request.session.get("oauth-token") map { token ⇒
          val user = userService.getUserByLogin(GetUserByLoginRequest(login = request.session.get("user").getOrElse("")))
          user.map(response ⇒ Ok(views.html.templates.home.index(user = response.user, libraries = libraries)))
        } getOrElse {
          Future.successful(Ok(views.html.templates.home.index(user = None, libraries = libraries, redirectUrl = Option(redirectUrl))).withSession("oauth-state" → state))
        }
      case -\/(ex) ⇒ Future.successful(InternalServerError(ex.getMessage))
    }
  }

  def library(libraryName: String) = Action.async { implicit request ⇒
    Future {
      exerciseOps.getLibraries.map(_.find(_.name == libraryName)).runTask match {
        case \/-(Some(library)) ⇒ Redirect(s"$libraryName/${library.sectionNames.head}")
        case _                  ⇒ Ok("Library not found")
      }
    }
  }

  def section(libraryName: String, sectionName: String) = Action.async { implicit request ⇒
    Future {
      val ops = for {
        libraries ← exerciseOps.getLibraries
        section ← exerciseOps.getSection(libraryName, sectionName)
      } yield (libraries.headOption, section)
      ops.runTask match {
        case \/-((Some(l), Some(s))) ⇒ Ok(views.html.templates.library.index(l, s))
        case \/-((Some(l), None))    ⇒ Redirect(l.sectionNames.head)
        case _                       ⇒ Ok("Section not found")
      }
    }
  }

  def javascriptRoutes = Action { implicit request ⇒
    Ok(
      JavaScriptReverseRouter("jsRoutes")(
        routes.javascript.ExercisesController.evaluate
      )
    ).as("text/javascript")
  }

}
