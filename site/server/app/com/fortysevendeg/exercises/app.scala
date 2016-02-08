package com.fortysevendeg.exercises

import cats.data.Coproduct

import com.fortysevendeg.shared.free.ExerciseOp
import com.fortysevendeg.exercises.services.free._

object app {
  type C01[A] = Coproduct[ExerciseOp, UserOp, A]
  type C02[A] = Coproduct[UserOp, UserProgressOp, A]
  type C03[A] = Coproduct[C01, C02, A]
  type ExercisesApp[A] = Coproduct[DBResult, C03, A]
}

