package com.fortysevendeg.exercises.services.interpreters

import cats._
import cats.free.Free
import com.fortysevendeg.exercises.app._
import com.fortysevendeg.exercises.models.UserDoobieStore
import com.fortysevendeg.exercises.persistence.repositories.UserProgressDoobieRepository
import com.fortysevendeg.exercises.services.free._
import com.fortysevendeg.shared.free._
import doobie.imports._

import scala.language.higherKinds
import scalaz.\/
import scalaz.concurrent.Task

/** Generic interpreters that can be lazily lifted via evidence of the target F via Applicative Pure Eval
  */
trait Interpreters[F[_]] extends InterpreterInstances[F] {
  def exerciseAndUserInterpreter(
    implicit
    A: Applicative[F],
    T: Transactor[F]
  ): ExercisesAndUserOps ~> F =
    exerciseOpsInterpreter or userOpsInterpreter

  def userAndUserProgressInterpreter(
    implicit
    A: Applicative[F],
    T: Transactor[F]
  ): UserAndUserProgressOps ~> F =
    userOpsInterpreter or userProgressOpsInterpreter

  def interpreters(
    implicit
    A: Applicative[F],
    T: Transactor[F]
  ): ExercisesApp ~> F =
    dbOpsInterpreter or exerciseAndUserInterpreter

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

  implicit def userOpsInterpreter(implicit A: Applicative[F], T: Transactor[F]): UserOp ~> F = new (UserOp ~> F) {

    def apply[A](fa: UserOp[A]): F[A] = fa match {
      case GetUsers()            ⇒ UserDoobieStore.all.transact(T)
      case GetUserByLogin(login) ⇒ UserDoobieStore.getByLogin(login).transact(T)
      case CreateUser(newUser)   ⇒ UserDoobieStore.create(newUser).transact(T)
      case UpdateUser(user)      ⇒ UserDoobieStore.update(user).map(_.isDefined).transact(T)
      case DeleteUser(user)      ⇒ UserDoobieStore.delete(user.id).transact(T)
    }
  }

  implicit def userProgressOpsInterpreter(
    implicit
    A: Applicative[F], T: Transactor[F]
  ): UserProgressOp ~> F = new (UserProgressOp ~> F) {

    def apply[A](fa: UserProgressOp[A]): F[A] = fa match {
      case UpdateUserProgress(userProgress) ⇒ UserProgressDoobieRepository.instance.create(userProgress).transact(T)
    }
  }

  implicit def dbOpsInterpreter(implicit A: Applicative[F]): DBResult ~> F = new (DBResult ~> F) {

    def apply[A](fa: DBResult[A]): F[A] = fa match {
      case _ ⇒ ???
      // case DBSuccess(value) ⇒ Task.now(value)
      // case DBFailure(error) ⇒ Task.fail(error)
    }
  }
}

trait InterpreterInstances[F[_]] { self: Interpreters[F] ⇒
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
object ProdInterpreters extends Interpreters[Task] {
  implicit class FreeOps[A](f: Free[ExercisesApp, A]) {

    /** Run this Free structure folding it's ops to an effect capturing task */
    def runTask(implicit T: Transactor[Task]): Throwable \/ A = {
      f.foldMap(interpreters).attemptRun
    }
  }
}

/** Test based interpreters lifting ops to their result identity **/
object TestInterpreters extends Interpreters[Id]

