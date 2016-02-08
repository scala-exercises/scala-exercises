package com.fortysevendeg.exercises.services.interpreters

import cats.data.{ Coproduct, Xor }
import cats._
import cats.free.Free

import scalaz.\/
import scalaz.concurrent.Task
import com.fortysevendeg.shared.free._
import com.fortysevendeg.exercises.models.UserDoobieStore

import doobie.imports._

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
    A: ApplicativeError[F, Throwable],
    T: Transactor[F]
  ): C01 ~> F =
    exerciseOpsInterpreter or userOpsInterpreter

  def userAndUserProgressInterpreter(
    implicit
    A: Applicative[F],
    T: Transactor[F]
  ): UserAndUserProgressOps ~> F =
    userOpsInterpreter or userProgressOpsInterpreter

  def interpreters(
    implicit
    A: ApplicativeError[F, Throwable],
    T: Transactor[F]
  ): ExercisesApp ~> F =
    dbOpsInterpreter or exerciseAndUserInterpreter

  /** Lifts Exercise Ops to an effect capturing Monad such as Task via natural transformations
    */
  implicit def exerciseOpsInterpreter(implicit A: ApplicativeError[F, Throwable]): ExerciseOp ~> F = new (ExerciseOp ~> F) {

    import com.fortysevendeg.exercises.services.ExercisesService._

    def apply[A](fa: ExerciseOp[A]): F[A] = fa match {
      case GetLibraries()                       ⇒ A.pureEval(Eval.later(libraries))
      case GetSection(libraryName, sectionName) ⇒ A.pureEval(Eval.later(section(libraryName, sectionName)))
      case Evaluate(evalInfo)                   ⇒ A.pureEval(Eval.later(evaluate(evalInfo)))
    }

  }

  implicit def userOpsInterpreter(implicit A: ApplicativeError[F, Throwable], T: Transactor[F]): UserOp ~> F = new (UserOp ~> F) {

    def apply[A](fa: UserOp[A]): Task[A] = fa match {
      case GetUsers()            ⇒ Task.fork(Task.delay(userService.all))
      case GetUserByLogin(login) ⇒ Task.fork(Task.delay(userService.getByLogin(login)))
      case CreateUser(newUser)   ⇒ Task.fork(Task.delay(userService.create(newUser)))
      case UpdateUser(user)      ⇒ Task.fork(Task.delay(userService.update(user)))
      case DeleteUser(user)      ⇒ Task.fork(Task.delay(userService.delete(user.id)))
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
      case DBSuccess(value) ⇒ A.pure(value)
      case DBFailure(error) ⇒ A.raiseError(error)
    }
  }

}

trait InterpreterInstances[F[_]] { self: Interpreters[F] ⇒
  implicit def idApplicativeError(
    implicit
    I: Applicative[Id]
  ): ApplicativeError[Id, Throwable] = new ApplicativeError[Id, Throwable] {
    override def pure[A](x: A): Id[A] = I.pure(x)

    override def ap[A, B](ff: Id[A ⇒ B])(fa: Id[A]): Id[B] = I.ap(ff)(fa)

    override def map[A, B](fa: Id[A])(f: Id[A ⇒ B]): Id[B] = I.map(fa)(f)

    override def product[A, B](fa: Id[A], fb: Id[B]): Id[(A, B)] = I.product(fa, fb)

    override def raiseError[A](e: Throwable): Id[A] =
      throw e

    override def handleErrorWith[A](fa: Id[A])(f: Throwable ⇒ Id[A]): Id[A] = {
      try {
        fa
      } catch {
        case e: Exception ⇒ f(e)
      }
    }
  }

  implicit val taskMonad: Monad[Task] with ApplicativeError[Task, Throwable] = new Monad[Task] with ApplicativeError[Task, Throwable] {

    def pure[A](x: A): Task[A] = Task.now(x)

    override def map[A, B](fa: Task[A])(f: A ⇒ B): Task[B] =
      fa map f

    override def flatMap[A, B](fa: Task[A])(f: A ⇒ Task[B]): Task[B] =
      fa flatMap f

    override def pureEval[A](x: Eval[A]): Task[A] =
      Task.fork(Task.delay(x.value))

    override def raiseError[A](e: Throwable): Task[A] =
      Task.fail(e)

    override def handleErrorWith[A](fa: Task[A])(f: Throwable ⇒ Task[A]): Task[A] =
      fa.handleWith({ case x ⇒ f(x) })
  }

}

/** Production based interpreters lifting ops to the effect capturing scalaz.concurrent.Task **/
object ProdInterpreters extends Interpreters[Task] {
  def scalazToCatsDisjunction[A, B](disj: \/[A, B]): Xor[A, B] =
    disj.fold(l ⇒ Xor.Left(l), r ⇒ Xor.Right(r))

  implicit class FreeOps[A](f: Free[ExercisesApp, A]) {

    /** Run this Free structure folding it's ops to an effect capturing task */
    def runTask(implicit T: Transactor[Task]): Throwable Xor A = {
      scalazToCatsDisjunction(f.foldMap(interpreters).attemptRun)
    }
  }
}

/** Test based interpreters lifting ops to their result identity **/
object TestInterpreters extends Interpreters[Id]

