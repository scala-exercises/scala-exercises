/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises.services.interpreters

import cats.{ Monad, Eval, ~>, ApplicativeError, Applicative }
import cats.data.Xor
import cats.free.Free

import com.fortysevendeg.exercises.app._
import com.fortysevendeg.exercises.persistence.repositories.{ UserRepository, UserProgressRepository }
import com.fortysevendeg.exercises.services.free._
import com.fortysevendeg.shared.free._
import doobie.imports._

import scala.language.higherKinds
import scalaz.\/
import scalaz.concurrent.Task
import FreeExtensions._

/** Generic interpreters that can be lazily lifted via evidence of the target F via Applicative Pure Eval
  */
trait Interpreters[M[_]] {

  implicit def interpreters(
    implicit
    A: ApplicativeError[M, Throwable],
    T: Transactor[M]
  ): ExercisesApp ~> M = {
    val exerciseAndUserInterpreter: C01 ~> M = exerciseOpsInterpreter or userOpsInterpreter
    val userAndUserProgressInterpreter: C02 ~> M = userProgressOpsInterpreter or exerciseAndUserInterpreter
    val all: ExercisesApp ~> M = dbOpsInterpreter or userAndUserProgressInterpreter
    all
  }

  /** Lifts Exercise Ops to an effect capturing Monad such as Task via natural transformations
    */
  implicit def exerciseOpsInterpreter(implicit A: ApplicativeError[M, Throwable]): ExerciseOp ~> M = new (ExerciseOp ~> M) {

    import com.fortysevendeg.exercises.services.ExercisesService._

    def apply[A](fa: ExerciseOp[A]): M[A] = fa match {
      case GetLibraries()                       ⇒ A.pureEval(Eval.later(libraries))
      case GetSection(libraryName, sectionName) ⇒ A.pureEval(Eval.later(section(libraryName, sectionName)))
      case Evaluate(evalInfo)                   ⇒ A.pureEval(Eval.later(evaluate(evalInfo)))
    }

  }

  implicit def userOpsInterpreter(implicit A: ApplicativeError[M, Throwable], T: Transactor[M], UR: UserRepository): UserOp ~> M = new (UserOp ~> M) {

    import UR._

    def apply[A](fa: UserOp[A]): M[A] = fa match {
      case GetUsers()            ⇒ all.transact(T)
      case GetUserByLogin(login) ⇒ getByLogin(login).transact(T)
      case CreateUser(newUser)   ⇒ create(newUser).transact(T)
      case UpdateUser(user)      ⇒ update(user).map(_.isDefined).transact(T)
      case DeleteUser(user)      ⇒ delete(user.id).transact(T)
    }
  }

  implicit def userProgressOpsInterpreter(
    implicit
    UPR: UserProgressRepository, T: Transactor[M]
  ): UserProgressOp ~> M = new (UserProgressOp ~> M) {

    def apply[A](fa: UserProgressOp[A]): M[A] = {
      fa match {
        case UpdateUserProgress(userProgress) ⇒
          UPR.create(userProgress).transact(T)
      }
    }
  }

  implicit def dbOpsInterpreter(implicit A: ApplicativeError[M, Throwable]): DBResult ~> M = new (DBResult ~> M) {

    def apply[A](fa: DBResult[A]): M[A] = fa match {
      case DBSuccess(value) ⇒ A.pure(value)
      case DBFailure(error) ⇒ A.raiseError(error)
    }
  }

}

/** Production based interpreters lifting ops to the effect capturing scalaz.concurrent.Task **/
trait ProdInterpreters extends Interpreters[Task] with TaskInstances

/** Test based interpreters lifting ops to their result identity **/
trait TestInterpreters extends Interpreters[cats.Id] with IdInstances

object FreeExtensions {

  def scalazToCatsDisjunction[A, B](disj: A \/ B): A Xor B =
    disj.fold(l ⇒ Xor.Left(l), r ⇒ Xor.Right(r))

  implicit class FreeOps[F[_], A](f: Free[F, A]) {

    def runF[M[_]: Monad](implicit interpreter: F ~> M) = f.foldMap(interpreter)

    def runTask(implicit interpreter: F ~> Task, T: Transactor[Task], M: Monad[Task]): Throwable Xor A = {
      scalazToCatsDisjunction(f.foldMap(interpreter).attemptRun)

    }
  }

}

trait TaskInstances {
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

trait IdInstances {
  implicit def idApplicativeError(
    implicit
    I: Applicative[cats.Id]
  ): ApplicativeError[cats.Id, Throwable] = new ApplicativeError[cats.Id, Throwable] {

    import cats.Id

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
}
