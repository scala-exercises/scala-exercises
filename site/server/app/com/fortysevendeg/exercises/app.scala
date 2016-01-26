package com.fortysevendeg.exercises

import cats.Monad
import cats.data.Coproduct
import cats.free.Free
import com.fortysevendeg.shared.free.ExerciseOp
import com.fortysevendeg.exercises.services.free._
import cats.~>
import scalaz.concurrent.Task
import com.fortysevendeg.exercises.services.interpreters.ProdInterpreters._
import scalaz.\/

object app {

  type ExercisesApp[A] = Coproduct[ExerciseOp, UserOp, A]

  implicit val interpreters: ExercisesApp ~> Task = exerciseOpsInterpreter or userOpsInterpreter

  implicit class FreeOps[A](f: Free[ExercisesApp, A]) {

    /** Run this Free structure folding it's ops to an effect capturing task */
    def runTask: Throwable \/ A =
      f.foldMap(interpreters).attemptRun

  }

}
