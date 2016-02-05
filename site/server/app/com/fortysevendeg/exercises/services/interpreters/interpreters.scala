package com.fortysevendeg.exercises.services.interpreters

import cats._
import cats.free.Free
import scalaz.concurrent.Task
import scala.language.higherKinds
import com.fortysevendeg.shared.free._
import com.fortysevendeg.exercises.services.free.UserOp

/** Generic interpreters that can be lazily lifted via evidence of the target F via Applicative Pure Eval
  */
trait Interpreters[F[_]] extends InterpreterInstances {

  /** Lifts Exercise Ops to an effect capturing Monad such as Task via natural transformations
    */
  implicit def exerciseOpsInterpreter(implicit A: Applicative[F]): ExerciseOp ~> F = new (ExerciseOp ~> F) {

    import com.fortysevendeg.exercises.services.ExercisesService._

    def apply[A](fa: ExerciseOp[A]): F[A] = fa match {
      case GetLibraries()                       ⇒ A.pureEval(Eval.later(libraries))
      case GetSection(libraryName, sectionName) ⇒ A.pureEval(Eval.later(section(libraryName, sectionName)))
      case Evaluate(evalInfo)                   ⇒ A.pureEval(Eval.later(evaluate(evalInfo)))
    }

  }

  implicit def userOpsInterpreter(implicit A: Applicative[F]): UserOp ~> F = new (UserOp ~> F) {

    def apply[A](fa: UserOp[A]): F[A] = ???

  }

}

trait InterpreterInstances {

  implicit val taskMonad: Monad[Task] = new Monad[Task] {

    def pure[A](x: A): Task[A] = Task.now(x)

    override def map[A, B](fa: Task[A])(f: A ⇒ B): Task[B] =
      fa map f

    override def flatMap[A, B](fa: Task[A])(f: A ⇒ Task[B]): Task[B] =
      fa flatMap f

    override def pureEval[A](x: Eval[A]): Task[A] =
      Task.fork(Task.delay(x.value))

  }

}

/** Production based interpreters lifting ops to the effect capturing scalaz.concurrent.Task **/
object ProdInterpreters extends Interpreters[Task]

/** Test based interpreters lifting ops to their result identity **/
object TestInterpreters extends Interpreters[Id]

