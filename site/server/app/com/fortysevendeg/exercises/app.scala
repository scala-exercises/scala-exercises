package com.fortysevendeg.exercises

import cats.{ ~>, Monad }
import cats.data.Coproduct
import cats.free.Free
import scalaz.concurrent.Task
import scalaz.\/

import com.fortysevendeg.shared.free.ExerciseOp
import com.fortysevendeg.exercises.services.free.UserOp

object app {
  type ExercisesApp[A] = Coproduct[ExerciseOp, UserOp, A]
}

