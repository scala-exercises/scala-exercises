/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises.controllers

import cats.data.Xor
import com.fortysevendeg.exercises.app._
import com.fortysevendeg.exercises.services.free.{ UserOps, UserProgressOps }
import com.fortysevendeg.shared.free.ExerciseOps
import com.fortysevendeg.exercises.services.interpreters.ProdInterpreters._
import doobie.imports._
import play.api.libs.json.Json
import play.api.mvc.Controller

import scalaz.concurrent.Task

class UserProgressController(
    implicit
    exerciseOps:     ExerciseOps[ExercisesApp],
    userOps:         UserOps[ExercisesApp],
    userProgressOps: UserProgressOps[ExercisesApp],
    T:               Transactor[Task]
) extends Controller with JsonFormats with AuthenticationModule {

  def fetchUserProgressBySection(libraryName: String, sectionName: String) =
    AuthenticatedUser { user ⇒
      userProgressOps.fetchUserProgressByLibrarySection(user, libraryName, sectionName).runTask match {
        case Xor.Right(response) ⇒ Ok(Json.toJson(response))
        case Xor.Left(error)     ⇒ BadRequest(error.getMessage)
      }
    }
}
