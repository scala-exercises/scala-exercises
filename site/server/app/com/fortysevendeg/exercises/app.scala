package com.fortysevendeg.exercises

import cats.data.Coproduct

import com.fortysevendeg.shared.free.ExerciseOp
import com.fortysevendeg.exercises.services.free._

object app {
  type ExercisesAndUserOps[A] = Coproduct[ExerciseOp, UserOp, A]
  type UserAndUserProgressOps[A] = Coproduct[UserOp, UserProgressOp, A]
  type ExercisesApp[A] = Coproduct[DBResult, ExercisesAndUserOps, A]
}

