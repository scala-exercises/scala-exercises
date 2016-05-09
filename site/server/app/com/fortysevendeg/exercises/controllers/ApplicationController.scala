/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises.controllers

import java.util.UUID
import com.fortysevendeg.exercises.services.interpreters.ProdInterpreters
import com.fortysevendeg.github4s.Github
import com.fortysevendeg.github4s.free.domain.Commit
import shared.{ Contribution, Contributor, Contributions }

import scala.collection.JavaConverters._

import cats.data.Xor
import com.fortysevendeg.exercises.app._
import com.fortysevendeg.exercises.services.free._
import com.fortysevendeg.exercises.services.ExercisesService
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
    Future {
      val ops = for {
        authorize ← githubOps.getAuthorizeUrl(OAuth2.githubAuthId, OAuth2.callbackUrl)
        libraries ← exerciseOps.getLibraries.map(ExercisesService.reorderLibraries(topLibraries, _))
        user ← userOps.getUserByLogin(request.session.get("user").getOrElse(""))
        progress ← userProgressOps.fetchMaybeUserProgress(user)
      } yield (libraries, user, request.session.get("oauth-token"), progress, authorize)

      ops.runTask match {
        case Xor.Right((libraries, user, Some(token), progress, _)) ⇒
          Ok(views.html.templates.home.index(user = user, libraries = libraries, progress = progress))
        case Xor.Right((libraries, None, None, progress, authorize)) ⇒
          Ok(views.html.templates.home.index(None, libraries, progress, Option(authorize.url))).withSession("oauth-state" → authorize.state)
        case Xor.Left(ex) ⇒ InternalServerError(ex.getMessage)
      }
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
        authorize ← githubOps.getAuthorizeUrl(OAuth2.githubAuthId, OAuth2.callbackUrl)
        libraries ← exerciseOps.getLibraries
        section ← exerciseOps.getSection(libraryName, sectionName)
        commits ← githubOps.getContributions(OAuth2.githubOwner, OAuth2.githubRepo, section.flatMap(_.path).getOrElse(""))
        contributions = commitsToContributions(commits)
        user ← userOps.getUserByLogin(request.session.get("user").getOrElse(""))
        libProgress ← userProgressOps.fetchMaybeUserProgressByLibrary(user, libraryName)
      } yield (libraries.find(_.name == libraryName), section, user, request.session.get("oauth-token"), libProgress, authorize, contributions)
      ops.runTask match {
        case Xor.Right((Some(l), Some(s), user, Some(token), libProgress, _, contributions)) ⇒ {
          Ok(
            views.html.templates.library.index(
              library = l,
              section = s,
              user = user,
              progress = libProgress,
              contributions = contributions,
              githubBaseUrl = OAuth2.githubOwner + "/" + OAuth2.githubRepo
            )
          )
        }
        case Xor.Right((Some(l), Some(s), user, None, libProgress, authorize, contributions)) ⇒ {
          Ok(
            views.html.templates.library.index(
              library = l,
              section = s,
              user = user,
              progress = libProgress,
              redirectUrl = Option(authorize.url),
              contributions = contributions,
              githubBaseUrl = OAuth2.githubOwner + "/" + OAuth2.githubRepo
            )
          ).withSession("oauth-state" → authorize.state)
        }
        case Xor.Right((Some(l), None, _, _, _, _, _)) ⇒ Redirect(l.sectionNames.head)
        case _                                         ⇒ Ok("Section not found")
      }
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

  private def commitsToContributions(commits: List[Commit]): Contributions =
    Contributions(toContribution(commits), commitsToContributors(commits))

  private def toContribution(commits: List[Commit]): List[Contribution] =
    commits.map(c ⇒ Contribution(c.sha, c.message, c.date, c.url, c.login, c.avatar_url, c.author_url))

  private def commitsToContributors(commits: List[Commit]): List[Contributor] = commits
    .groupBy(c ⇒ (c.login, c.avatar_url, c.author_url))
    .keys
    .map { case (login, avatar, url) ⇒ Contributor(login, avatar, url) }
    .toList

}
