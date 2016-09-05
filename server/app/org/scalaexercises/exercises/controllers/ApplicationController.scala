/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.exercises.controllers

import org.scalaexercises.exercises.Secure

import java.util.UUID
import cats.free.Free
import play.api.cache.{ Cache, CacheApi }
import org.scalaexercises.types.exercises.{ Contribution, Contributor }
import scala.collection.JavaConverters._

import cats.data.Xor

import org.scalaexercises.exercises.utils.OAuth2

import org.scalaexercises.algebra.app._
import org.scalaexercises.algebra.user.UserOps
import org.scalaexercises.algebra.progress.UserProgressOps
import org.scalaexercises.algebra.exercises.ExerciseOps
import org.scalaexercises.types.github.Repository
import org.scalaexercises.algebra.github.GithubOps

import org.scalaexercises.exercises.services.ExercisesService
import org.scalaexercises.exercises.services.interpreters.ProdInterpreters

import doobie.imports._
import play.api.{ Play, Application, Logger }
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import play.api.routing.JavaScriptReverseRouter

import scala.concurrent.Future
import scala.concurrent.duration._
import scalaz.concurrent.Task
import org.scalaexercises.exercises.services.interpreters.FreeExtensions._

class ApplicationController(cache: CacheApi)(
    implicit
    exerciseOps:     ExerciseOps[ExercisesApp],
    userOps:         UserOps[ExercisesApp],
    userProgressOps: UserProgressOps[ExercisesApp],
    githubOps:       GithubOps[ExercisesApp],
    T:               Transactor[Task]
) extends Controller with AuthenticationModule with ProdInterpreters {
  implicit def application: Application = Play.current

  lazy val topLibraries: List[String] = application.configuration.getStringList("exercises.top_libraries") map (_.asScala.toList) getOrElse Nil

  val MainRepoCacheKey = "scala-exercises.repo"

  /** cache the main repo stars, forks and watchers info for 30 mins */
  private[this] def scalaexercisesRepo: Future[Repository] = {
    cache.get[Repository](MainRepoCacheKey) match {
      case Some(repo) ⇒ Future.successful(repo)
      case None ⇒
        githubOps.getRepository(OAuth2.githubSiteOwner, OAuth2.githubSiteRepo).runFuture flatMap {
          case Xor.Right(repo) ⇒
            cache.set(MainRepoCacheKey, repo, 30 minutes)
            Future.successful(repo)
          case Xor.Left(err) ⇒ {
            Logger.error("Error fetching scala-exercises repository information", err)
            Future.failed[Repository](err)
          }
        }
    }
  }

  def index = Secure(Action.async { implicit request ⇒

    val ops = for {
      authorize ← githubOps.getAuthorizeUrl(OAuth2.githubAuthId, OAuth2.callbackUrl)
      libraries ← exerciseOps.getLibraries.map(ExercisesService.reorderLibraries(topLibraries, _))
      user ← userOps.getUserByLogin(request.session.get("user").getOrElse(""))
      progress ← userProgressOps.fetchMaybeUserProgress(user)
    } yield (libraries, user, request.session.get("oauth-token"), progress, authorize)

    for {
      repo ← scalaexercisesRepo
      result ← ops.runFuture map {
        case Xor.Right((libraries, user, Some(token), progress, _))  ⇒ Ok(views.html.templates.home.index(user = user, libraries = libraries, progress = progress, repo = repo))
        case Xor.Right((libraries, None, None, progress, authorize)) ⇒ Ok(views.html.templates.home.index(user = None, libraries = libraries, progress = progress, redirectUrl = Option(authorize.url), repo = repo)).withSession("oauth-state" → authorize.state)
        case Xor.Right((libraries, Some(user), None, _, _))          ⇒ InternalServerError("Session token not found")
        case Xor.Left(ex) ⇒ {
          Logger.error("Error rendering index page", ex)
          InternalServerError(ex.getMessage)
        }
      }
    } yield result
  })

  def library(libraryName: String) = Secure(Action.async { implicit request ⇒
    val ops = for {
      library ← exerciseOps.getLibrary(libraryName)
      user ← userOps.getUserByLogin(request.session.get("user").getOrElse(""))
      section ← user.fold(
        Free.pure(None): Free[ExercisesApp, Option[String]]
      )(usr ⇒ userProgressOps.getLastSeenSection(usr, libraryName))
    } yield (library, user, section)

    ops.runFuture map {
      case Xor.Right((Some(library), _, Some(sectionName))) if library.sectionNames.contains(sectionName) ⇒
        Redirect(s"$libraryName/$sectionName")
      case Xor.Right((Some(library), _, _)) if library.sectionNames.nonEmpty ⇒
        Redirect(s"$libraryName/${library.sectionNames.head}")
      case Xor.Right((None, _, _)) ⇒ NotFound("Library not found")
      case Xor.Left(ex) ⇒ {
        Logger.error(s"Error rendering library: $libraryName", ex)
        InternalServerError(ex.getMessage)
      }
    }
  })

  def section(libraryName: String, sectionName: String) = Secure(Action.async { implicit request ⇒
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
      case Xor.Right((_, _, _, _, _, _, _))          ⇒ InternalServerError("Library and section not found")
      case Xor.Left(ex) ⇒ {
        Logger.error(s"Error rendering section: $libraryName/$sectionName", ex)
        InternalServerError(ex.getMessage)
      }
    }
  })

  def javascriptRoutes = Secure(Action { implicit request ⇒
    import routes.javascript._
    Ok(
      JavaScriptReverseRouter("jsRoutes")(
        ExercisesController.evaluate,
        UserProgressController.fetchUserProgressBySection
      )
    ).as("text/javascript")
  })

  private def toContributors(contributions: List[Contribution]): List[Contributor] = contributions
    .groupBy(c ⇒ (c.author, c.authorUrl, c.avatarUrl))
    .keys
    .map { case (author, authorUrl, avatarUrl) ⇒ Contributor(author, authorUrl, avatarUrl) }
    .toList

}
