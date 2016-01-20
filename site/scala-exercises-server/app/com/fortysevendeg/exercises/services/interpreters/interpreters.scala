package com.fortysevendeg.exercises.services.interpreters

import cats.{ ~>, Monad }
import cats.free.Free
import scalaz.concurrent.Task
import scala.language.higherKinds
import com.fortysevendeg.shared.free._
import com.fortysevendeg.exercises.services.free.UserOp

object ProdInterpreters {

  /** Lifts Exercise Ops to an effect capturing Monad such as Task via natural transformations
    */
  implicit def exerciseOpsInterpreter: ExerciseOp ~> Task = new (ExerciseOp ~> Task) {

    import com.fortysevendeg.exercises.services.ExercisesService._

    def apply[A](fa: ExerciseOp[A]): Task[A] = fa match {
      case GetLibraries()                       ⇒ Task.fork(Task.delay(libraries))
      case GetSection(libraryName, sectionName) ⇒ Task.fork(Task.delay(section(libraryName, sectionName)))
      case Evaluate(evalInfo)                   ⇒ Task.fork(Task.delay(evaluate(evalInfo)))
    }

  }

  implicit def userOpsInterpreter: UserOp ~> Task = new (UserOp ~> Task) {

    def apply[A](fa: UserOp[A]): Task[A] = ???

  }

}
