package com.fortysevendeg.exercises.controllers

import java.util.UUID

import cats.data.Xor
import com.fortysevendeg.exercises.app._
import com.fortysevendeg.exercises.services.free.UserOps
import com.fortysevendeg.exercises.services.interpreters.ProdInterpreters._
import com.fortysevendeg.exercises.utils.OAuth2
import com.fortysevendeg.shared.free.ExerciseOps
import doobie.imports._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import play.api.routing.JavaScriptReverseRouter

import scala.concurrent.Future
import scalaz.concurrent.Task

class ApplicationController(
    implicit
    exerciseOps: ExerciseOps[ExercisesApp],
    userOps:     UserOps[ExercisesApp],
    T:           Transactor[Task]
) extends Controller {

  def index = Action.async { implicit request ⇒

    val scope = "user"
    val state = UUID.randomUUID().toString
    val callbackUrl = com.fortysevendeg.exercises.utils.routes.OAuth2Controller.callback(None, None).absoluteURL()
    val logoutUrl = com.fortysevendeg.exercises.utils.routes.OAuth2Controller.logout().absoluteURL()
    val redirectUrl = OAuth2.getAuthorizationUrl(callbackUrl, scope, state)

    val ops = for {
      libraries ← exerciseOps.getLibraries
      user ← userOps.getUserByLogin(request.session.get("user").getOrElse(""))
    } yield (libraries, user)

    ops.runTask match {
      case Xor.Right((libraries, user)) ⇒
        request.session.get("oauth-token") map { token ⇒
          Future.successful(Ok(views.html.templates.home.index(user = user, libraries = libraries)))
        } getOrElse {
          Future.successful(Ok(views.html.templates.home.index(user = None, libraries = libraries, redirectUrl = Option(redirectUrl))).withSession("oauth-state" → state))
        }
      case Xor.Left(ex) ⇒ Future.successful(InternalServerError(ex.getMessage))
    }
  }

  def library(libraryName: String) = Action.async { implicit request ⇒
    Future {
      exerciseOps.getLibraries.map(_.find(_.name == libraryName)).runTask match {
        case Xor.Right(Some(library)) ⇒ Redirect(s"$libraryName/${library.sectionNames.head}")
        case _                        ⇒ Ok("Library not found")
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
        case Xor.Right((Some(l), Some(s))) ⇒ Ok(views.html.templates.library.index(l, s))
        case Xor.Right((Some(l), None))    ⇒ Redirect(l.sectionNames.head)
        case _                             ⇒ Ok("Section not found")
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
