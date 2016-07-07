/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.exercises.controllers

import org.scalaexercises.exercises.Secure

import java.util.UUID
import cats.free.Free

import cats.data.Xor

import org.scalaexercises.algebra.app._
import org.scalaexercises.algebra.exercises.ExerciseOps
import org.scalaexercises.types.exercises.{ Contribution, Contributor }

import org.scalaexercises.exercises.services.ExercisesService
import org.scalaexercises.exercises.services.interpreters.ProdInterpreters
import org.scalaexercises.exercises.utils.OAuth2

import doobie.imports._

import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._

import scala.concurrent.Future
import scalaz.concurrent.Task
import org.scalaexercises.exercises.services.interpreters.FreeExtensions._

class SitemapController(
    implicit
    exerciseOps: ExerciseOps[ExercisesApp],
    T:           Transactor[Task]
) extends Controller with ProdInterpreters {

  def sitemap = Secure(Action.async { implicit request ⇒
    exerciseOps.getLibraries.runFuture map {
      case Xor.Right(libraries) ⇒ Ok(views.xml.templates.sitemap.sitemap(libraries = libraries))
      case Xor.Left(ex) ⇒ {
        Logger.error("Error rendering sitemap", ex)
        InternalServerError(ex.getMessage)
      }
    }
  })

}
