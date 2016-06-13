/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises.controllers

import java.util.UUID
import cats.free.Free
import com.fortysevendeg.github4s.Github
import shared.{ Contribution, Contributor }
import scala.collection.JavaConverters._

import cats.data.Xor
import com.fortysevendeg.exercises.app._
import com.fortysevendeg.exercises.services.free._
import com.fortysevendeg.exercises.services.ExercisesService
import com.fortysevendeg.exercises.services.interpreters.ProdInterpreters
import com.fortysevendeg.exercises.utils.OAuth2
import com.fortysevendeg.shared.free.ExerciseOps
import doobie.imports._
import play.api.{ Play, Application }
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import play.api.routing.JavaScriptReverseRouter

import scala.concurrent.Future
import scalaz.concurrent.Task
import com.fortysevendeg.exercises.services.interpreters.FreeExtensions._

class ApplicationController(
    implicit
    exerciseOps:     ExerciseOps[ExercisesApp],
    userOps:         UserOps[ExercisesApp],
    userProgressOps: UserProgressOps[ExercisesApp],
    githubOps:       GithubOps[ExercisesApp],
    T:               Transactor[Task]
) extends Controller with AuthenticationModule with ProdInterpreters {
  implicit def application: Application = Play.current

  lazy val topLibraries: List[String] = application.configuration.getStringList("exercises.top_libraries") map (_.asScala.toList) getOrElse Nil

  def index = Action.async { implicit request ⇒

    val ops = for {
      authorize ← githubOps.getAuthorizeUrl(OAuth2.githubAuthId, OAuth2.callbackUrl)
      libraries ← exerciseOps.getLibraries.map(ExercisesService.reorderLibraries(topLibraries, _))
      user ← userOps.getUserByLogin(request.session.get("user").getOrElse(""))
      progress ← userProgressOps.fetchMaybeUserProgress(user)
    } yield (libraries, user, request.session.get("oauth-token"), progress, authorize)

    ops.runFuture map {
      case Xor.Right((libraries, user, Some(token), progress, _)) ⇒ Ok(views.html.templates.home.index(user = user, libraries = libraries, progress = progress))
      case Xor.Right((libraries, None, None, progress, authorize)) ⇒ Ok(views.html.templates.home.index(user = None, libraries = libraries, progress = progress, redirectUrl = Option(authorize.url))).withSession("oauth-state" → authorize.state)
      case Xor.Left(ex) ⇒ InternalServerError(ex.getMessage)
    }
  }

  def library(libraryName: String) = Action.async { implicit request ⇒
    exerciseOps.getLibraries.map(_.find(_.name == libraryName)).runFuture map {
      case Xor.Right(Some(library)) ⇒ Redirect(s"$libraryName/${library.sectionNames.head}")
      case _                        ⇒ Ok("Library not found")
    }
  }

  def section(libraryName: String, sectionName: String) = Action.async { implicit request ⇒
    val ops = for {
      authorize ← githubOps.getAuthorizeUrl(OAuth2.githubAuthId, OAuth2.callbackUrl)
      library ← exerciseOps.getLibrary(libraryName)
      section ← exerciseOps.getSection(libraryName, sectionName)
      contributors = toContributors(section.fold(List.empty[Contribution])(s ⇒ s.contributions))
      user ← userOps.getUserByLogin(request.session.get("user").getOrElse(""))
      libProgress ← userProgressOps.fetchMaybeUserProgressByLibrary(user, libraryName)
    } yield (library, section, user, request.session.get("oauth-token"), libProgress, authorize, contributors)

    ops.runFuture map {
      case Xor.Right((Some(l), Some(s), user, Some(token), libProgress, _, contributors)) ⇒ {
        Ok(
          views.html.templates.library.index(
            library = l,
            section = s,
            user = user,
            progress = libProgress,
            contributors = contributors
          )
        )
      }
      case Xor.Right((Some(l), Some(s), user, None, libProgress, authorize, contributors)) ⇒ {
        Ok(
          views.html.templates.library.index(
            library = l,
            section = s,
            user = user,
            progress = libProgress,
            redirectUrl = Option(authorize.url),
            contributors = contributors
          )
        ).withSession("oauth-state" → authorize.state)
      }
      case Xor.Right((Some(l), None, _, _, _, _, _)) ⇒ NotFound("Section not found")
      case Xor.Right((None, _, _, _, _, _, _))       ⇒ NotFound("Library not found")
      case Xor.Left(ex)                              ⇒ InternalServerError(ex.getMessage)
    }
  }

  def javascriptRoutes = Action { implicit request ⇒
    import routes.javascript._
    Ok(
      JavaScriptReverseRouter("jsRoutes")(
        ExercisesController.evaluate,
        UserProgressController.fetchUserProgressBySection
      )
    ).as("text/javascript")
  }

  private def toContributors(contributions: List[Contribution]): List[Contributor] = contributions
    .groupBy(c ⇒ (c.author, c.authorUrl, c.avatarUrl))
    .keys
    .map { case (author, authorUrl, avatarUrl) ⇒ Contributor(author, authorUrl, avatarUrl) }
    .toList

}
