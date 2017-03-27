/*
 * scala-exercises - core
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.algebra

import org.scalaexercises.algebra.user.UserOps
import org.scalaexercises.algebra.exercises.ExerciseOps
import org.scalaexercises.algebra.progress.UserProgressOps
import org.scalaexercises.algebra.github.GithubOps

import freestyle._
import freestyle.implicits._

object app {

  @module
  trait ExercisesApp[F[_]] {
    val exerciseOps: ExerciseOps[F]
    val userOps: UserOps[F]
    val userProgressOps: UserProgressOps[F]
    val githubOps: GithubOps[F]
  }

}
