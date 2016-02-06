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

  def userOpsInterpreter: UserOp ~> Task = new (UserOp ~> Task) {
    val userService = new UserServiceImpl

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
    ???
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

