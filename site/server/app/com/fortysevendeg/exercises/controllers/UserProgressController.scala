/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises.controllers

import com.fortysevendeg.exercises.app._
import com.fortysevendeg.exercises.services.free.{ UserOps, UserProgressOps }
import com.fortysevendeg.shared.free.ExerciseOps
import doobie.imports._
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
    AuthenticatedUser {
      user ⇒ ???
    }
}
