/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaExercises.exercises.controllers

import org.scalaExercises.exercises.Secure

import java.util.UUID
import cats.free.Free
import shared.{ Contribution, Contributor }

import cats.data.Xor
import org.scalaExercises.exercises.app._
import org.scalaExercises.exercises.services.free._
import org.scalaExercises.exercises.services.ExercisesService
import org.scalaExercises.exercises.services.interpreters.ProdInterpreters
import org.scalaExercises.exercises.utils.OAuth2
import org.scalaExercises.shared.free.ExerciseOps
import doobie.imports._
import play.api.{ Play, Application }
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._

import scala.concurrent.Future
import scalaz.concurrent.Task
import org.scalaExercises.exercises.services.interpreters.FreeExtensions._

class SitemapController(
    implicit
    exerciseOps: ExerciseOps[ExercisesApp],
    T:           Transactor[Task]
) extends Controller with ProdInterpreters {

  def sitemap = Secure(Action.async { implicit request ⇒
    exerciseOps.getLibraries.runFuture map {
      case Xor.Right(libraries) ⇒ Ok(views.xml.templates.sitemap.sitemap(libraries = libraries))
      case Xor.Left(ex)         ⇒ InternalServerError(ex.getMessage)
    }
  })

}
