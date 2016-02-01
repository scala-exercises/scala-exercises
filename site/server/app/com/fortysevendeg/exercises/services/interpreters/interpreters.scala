package com.fortysevendeg.exercises.services.interpreters

import play.api.{ Play }
import cats.data.Coproduct
import cats.Id
import cats.{ ~>, Monad }
import cats.free.Free
import scalaz.\/
import scalaz.concurrent.Task
import scala.language.higherKinds
import com.fortysevendeg.shared.free._
import com.fortysevendeg.exercises.app._
import com.fortysevendeg.exercises.services.{ UserServices }
import com.fortysevendeg.exercises.services.free._

class ProdInterpreters(implicit userService: UserServices) {
  val interpreters: ExercisesApp ~> Task = exerciseOpsInterpreter or userOpsInterpreter

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

    def apply[A](fa: UserOp[A]): Task[A] = fa match {
      case GetUsers()            ⇒ Task.delay(userService.all)
      case GetUserByLogin(login) ⇒ Task.delay(userService.getUserByLogin(login))
      case CreateUser(
        login,
        name,
        github_id,
        picture_url,
        github_url,
        email) ⇒ Task.delay(userService.createUser(login, name, github_id, picture_url, github_url, email))
      case UpdateUser(
        id,
        login,
        name,
        github_id,
        picture_url,
        github_url,
        email
        ) ⇒ Task.delay(userService.update(
        id,
        login,
        name,
        github_id,
        picture_url,
        github_url,
        email
      ))
      case DeleteUser(id) ⇒ Task.delay(userService.delete(id))
    }
  }
}

object ProdInterpreters {
  implicit def instance(implicit U: UserServices): ProdInterpreters = new ProdInterpreters

  implicit val taskMonad: Monad[Task] = new Monad[Task] {

    def pure[A](x: A): Task[A] = Task.now(x)

    override def map[A, B](fa: Task[A])(f: A ⇒ B): Task[B] =
      fa map f

    override def flatMap[A, B](fa: Task[A])(f: A ⇒ Task[B]): Task[B] =
      fa flatMap f

  }

  implicit class FreeOps[A](f: Free[ExercisesApp, A]) {

    /** Run this Free structure folding it's ops to an effect capturing task */
    def runTask(implicit U: UserServices): Throwable \/ A = {
      f.foldMap(instance.interpreters).attemptRun
    }
  }
}
