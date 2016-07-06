/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.algebra

import cats.data.Coproduct

import org.scalaexercises.algebra.user.UserOp
import org.scalaexercises.algebra.exercises.ExerciseOp
import org.scalaexercises.algebra.progress.UserProgressOp
import org.scalaexercises.algebra.github.GithubOp

object app {
  type C01[A] = Coproduct[ExerciseOp, UserOp, A]
  type C02[A] = Coproduct[UserProgressOp, C01, A]
  type ExercisesApp[A] = Coproduct[GithubOp, C02, A]
}
