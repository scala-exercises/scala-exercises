/*
 *  scala-exercises
 *
 *  Copyright 2015-2017 47 Degrees, LLC. <http://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.scalaexercises.exercises.controllers

import org.scalaexercises.exercises.Secure
import cats.free._
import cats.instances.future._
import play.api.cache.CacheApi
import org.scalaexercises.types.exercises.{Contribution, Contributor}

import scala.collection.JavaConverters._
import org.scalaexercises.exercises.utils.ConfigUtils
import org.scalaexercises.algebra.app._
import org.scalaexercises.algebra.user.UserOps
import org.scalaexercises.algebra.progress.{UserExercisesProgress, UserProgressOps}
import org.scalaexercises.algebra.exercises.ExerciseOps
import org.scalaexercises.types.github.Repository
import org.scalaexercises.algebra.github.GithubOps
import org.scalaexercises.exercises.services.ExercisesService
import org.scalaexercises.exercises.services.interpreters.ProdInterpreters
import doobie.imports._
import play.api.{Application, Logger, Play}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import play.api.routing.JavaScriptReverseRouter

import scala.concurrent.Future
import scala.concurrent.duration._
import scalaz.concurrent.Task
import freestyle._
import freestyle.implicits._

class ApplicationController(cache: CacheApi)(
    implicit exerciseOps: ExerciseOps[ExercisesApp.Op],
    userOps: UserOps[ExercisesApp.Op],
    userProgressOps: UserProgressOps[ExercisesApp.Op],
    userExercisesProgress: UserExercisesProgress[ExercisesApp.Op],
    githubOps: GithubOps[ExercisesApp.Op],
    T: Transactor[Task]
) extends Controller
    with AuthenticationModule
    with ProdInterpreters {
  implicit def application: Application = Play.current

  lazy val topLibraries: List[String] = application.configuration.getStringList(
    "exercises.top_libraries") map (_.asScala.toList) getOrElse Nil

  val MainRepoCacheKey = "scala-exercises.repo"

  /** cache the main repo stars, forks and watchers info for 30 mins */
  private[this] def scalaexercisesRepo: FreeS[ExercisesApp.Op, Repository] = {
    cache.get[Repository](MainRepoCacheKey) match {
      case Some(repo) ⇒ FreeS.pure(repo)
      case None ⇒
        githubOps
          .getRepository(
            ConfigUtils.githubSiteOwner,
            ConfigUtils.githubSiteRepo
          ) match {
          case repo ⇒
            cache.set(MainRepoCacheKey, repo, 30 minutes)
            repo
          case err ⇒
            Logger.error("Error fetching scala-exercises repository information")
            err
        }
    }
  }

  def index =
    Secure(Action.async { implicit request ⇒
      for {
        authorize ← githubOps.getAuthorizeUrl(ConfigUtils.githubAuthId, ConfigUtils.callbackUrl)
        libraries ← exerciseOps.getLibraries.map(
          ExercisesService.reorderLibraries(topLibraries, _))
        user     ← userOps.getUserByLogin(request.session.get("user").getOrElse(""))
        progress ← userExercisesProgress.fetchMaybeUserProgress(user)
        repo     ← scalaexercisesRepo
        result = (libraries, user, request.session.get("oauth-token"), progress, authorize) match {
          case (libraries, user, Some(token), progress, _) ⇒
            Ok(
              views.html.templates.home
                .index(user = user, libraries = libraries, progress = progress, repo = repo))
          case (libraries, None, None, progress, authorize) ⇒
            Ok(
              views.html.templates.home.index(
                user = None,
                libraries = libraries,
                progress = progress,
                redirectUrl = Option(authorize.url),
                repo = repo)).withSession("oauth-state" → authorize.state)
          case (libraries, Some(user), None, _, _) ⇒ Unauthorized("Session token not found")

        }
      } yield result
    })

  def library(libraryName: String) =
    Secure(Action.async { implicit request ⇒
      val ops = for {
        library ← exerciseOps.getLibrary(libraryName)
        user    ← userOps.getUserByLogin(request.session.get("user").getOrElse(""))
        section ← user.fold(
          Free.liftF[FreeApplicative[ExercisesApp.Op, ?], Option[String]](
            FreeApplicative.pure(
              None: Option[String]
            ): FreeApplicative[ExercisesApp.Op, Option[String]]
          )
        )(usr ⇒ userProgressOps.getLastSeenSection(usr, libraryName))
      } yield (library, user, section)

      ops map {
        case (Some(library), _, Some(sectionName)) if library.sectionNames.contains(sectionName) ⇒
          Redirect(s"$libraryName/$sectionName")
        case (Some(library), _, _) if library.sectionNames.nonEmpty ⇒
          Redirect(s"$libraryName/${library.sectionNames.head}")
        case (None, _, _) ⇒ NotFound("Library not found")
      }
    })

  def section(libraryName: String, sectionName: String) =
    Secure(Action.async { implicit request ⇒
      val ops = for {
        authorize ← githubOps.getAuthorizeUrl(ConfigUtils.githubAuthId, ConfigUtils.callbackUrl)
        library   ← exerciseOps.getLibrary(libraryName)
        section   ← exerciseOps.getSection(libraryName, sectionName)
        contributors = toContributors(section.fold(List.empty[Contribution])(s ⇒ s.contributions))
        user        ← userOps.getUserByLogin(request.session.get("user").getOrElse(""))
        libProgress ← userExercisesProgress.fetchMaybeUserProgressByLibrary(user, libraryName)
      } yield
        (
          library,
          section,
          user,
          request.session.get("oauth-token"),
          libProgress,
          authorize,
          contributors)

      ops map {
        case (Some(l), Some(s), user, Some(token), libProgress, _, contributors) ⇒
          Ok(
            views.html.templates.library.index(
              library = l,
              section = s,
              user = user,
              progress = libProgress,
              contributors = contributors
            )
          )
        case (Some(l), Some(s), user, None, libProgress, authorize, contributors) ⇒
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
        case (Some(l), None, _, _, _, _, _) ⇒ NotFound("Section not found")
        case (None, _, _, _, _, _, _)       ⇒ NotFound("Library not found")
        case (_, _, _, _, _, _, _)          ⇒ NotFound("Library and section not found")

      }
    })

  def javascriptRoutes =
    Secure(Action { implicit request ⇒
      import routes.javascript._
      Ok(
        JavaScriptReverseRouter("jsRoutes")(
          ExercisesController.evaluate,
          UserProgressController.fetchUserProgressBySection
        )
      ).as("text/javascript")
    })

  private def toContributors(contributions: List[Contribution]): List[Contributor] =
    contributions.map(c ⇒ Contributor(c.author, c.authorUrl, c.avatarUrl)).distinct

  def onHandlerNotFound(route: String) = Action { implicit request ⇒
    if (route.endsWith("/")) {
      MovedPermanently("/" + request.path.take(request.path.length - 1).dropWhile(_ == '/'))
    } else {
      NotFound
    }
  }
}
