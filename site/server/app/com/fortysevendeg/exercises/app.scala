package com.fortysevendeg.exercises

import cats.data.Coproduct

import com.fortysevendeg.shared.free.ExerciseOp
import com.fortysevendeg.exercises.services.free.UserOp

object app {
  type ExercisesApp[A] = Coproduct[ExerciseOp, UserOp, A]
}

