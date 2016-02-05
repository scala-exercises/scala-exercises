package com.fortysevendeg.exercises.services.interpreters

import play.api.{ Play }
import cats.data.Coproduct
import doobie.imports._
import cats.Id
import cats.{ ~>, Monad }
import cats.free.Free
import scalaz.\/
import scalaz.concurrent.Task
import scala.language.higherKinds
import com.fortysevendeg.shared.free._
import com.fortysevendeg.exercises.app._
import com.fortysevendeg.exercises.services.UserServiceImpl
import com.fortysevendeg.exercises.services.free._

class ProdInterpreters(implicit transactor: Transactor[Task]) {
  val exerciseAndUserInterpreter: ExercisesAndUserOps ~> Task =
    exerciseOpsInterpreter or userOpsInterpreter

  val interpreters: ExercisesApp ~> Task =
    dbOpsInterpreter or exerciseAndUserInterpreter

  /** Lifts Exercise Ops to an effect capturing Monad such as Task via natural transformations
    */
  def exerciseOpsInterpreter: ExerciseOp ~> Task = new (ExerciseOp ~> Task) {

    import com.fortysevendeg.exercises.services.ExercisesService._

    def apply[A](fa: ExerciseOp[A]): Task[A] = fa match {
      case GetLibraries()                       ⇒ Task.fork(Task.delay(libraries))
      case GetSection(libraryName, sectionName) ⇒ Task.fork(Task.delay(section(libraryName, sectionName)))
      case Evaluate(evalInfo)                   ⇒ Task.fork(Task.delay(evaluate(evalInfo)))
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

  def dbOpsInterpreter: DBResult ~> Task = new (DBResult ~> Task) {

    def apply[A](fa: DBResult[A]): Task[A] = fa match {
      case DBSuccess(value) ⇒ Task.now(value)
      case DBFailure(error) ⇒ Task.fail(error)
    }
  }
}

object ProdInterpreters {
  implicit def instance(implicit T: Transactor[Task]): ProdInterpreters = new ProdInterpreters

  implicit val taskMonad: Monad[Task] = new Monad[Task] {

    def pure[A](x: A): Task[A] = Task.now(x)

    override def map[A, B](fa: Task[A])(f: A ⇒ B): Task[B] =
      fa map f

    override def flatMap[A, B](fa: Task[A])(f: A ⇒ Task[B]): Task[B] =
      fa flatMap f

  }

  implicit class FreeOps[A](f: Free[ExercisesApp, A]) {

    /** Run this Free structure folding it's ops to an effect capturing task */
    def runTask(implicit T: Transactor[Task]): Throwable \/ A = {
      f.foldMap(instance.interpreters).attemptRun
    }
  }
}
