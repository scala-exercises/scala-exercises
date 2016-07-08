/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.exercises.controllers

import cats.data.Xor

import org.scalaexercises.algebra.app._
import org.scalaexercises.algebra.user.UserOps
import org.scalaexercises.algebra.progress.UserProgressOps

import org.scalaexercises.algebra.exercises.ExerciseOps
import org.scalaexercises.exercises.services.interpreters.ProdInterpreters

import doobie.imports._

import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.Controller

import scalaz.concurrent.Task

import scala.concurrent.ExecutionContext.Implicits.global

import org.scalaexercises.exercises.services.interpreters.FreeExtensions._

class UserProgressController(
    implicit
    exerciseOps:     ExerciseOps[ExercisesApp],
    userOps:         UserOps[ExercisesApp],
    userProgressOps: UserProgressOps[ExercisesApp],
    T:               Transactor[Task]
) extends Controller with JsonFormats with AuthenticationModule with ProdInterpreters {

  def fetchUserProgressBySection(libraryName: String, sectionName: String) =
    AuthenticatedUser { user ⇒
      userProgressOps.fetchUserProgressByLibrarySection(user, libraryName, sectionName).runFuture map {
        case Xor.Right(response) ⇒ Ok(Json.toJson(response))
        case Xor.Left(error) ⇒
          Logger.error(s"Error while fetching user progress for $libraryName/$sectionName", error)
          BadRequest(error.getMessage)
      }
    }
}
