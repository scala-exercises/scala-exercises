/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.exercises

import cats.data.Coproduct

import org.scalaexercises.shared.free.ExerciseOp
import org.scalaexercises.exercises.services.free._

object app {
  type C01[A] = Coproduct[ExerciseOp, UserOp, A]
  type C02[A] = Coproduct[UserProgressOp, C01, A]
  type C03[A] = Coproduct[GithubOp, C02, A]
  type ExercisesApp[A] = Coproduct[DBResult, C03, A]
}
