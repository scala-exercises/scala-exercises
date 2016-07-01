/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.exercises.services.interpreters

import org.scalaexercises.algebra.app._
import org.scalaexercises.algebra.user._
import org.scalaexercises.algebra.exercises._
import org.scalaexercises.algebra.progress._
import org.scalaexercises.algebra.github._
import org.scalaexercises.types.github._

import org.scalaexercises.exercises.persistence.repositories.{ UserRepository, UserProgressRepository }

import github4s.app.GitHub4s
import github4s.free.interpreters.{ Interpreters ⇒ GithubInterpreters }
import github4s.Github
import Github._
import github4s.implicits._
import github4s.GithubResponses.{ GHResult, GHResponse }

import cats._
import cats.data.Xor
import cats.free.Free

import doobie.imports._

import scala.concurrent.{ Future, Promise }

import scala.language.higherKinds
import scalaz.\/
import scalaz.concurrent.Task
import FreeExtensions._

/** Generic interpreters that can be lazily lifted via evidence of the target F via Applicative Pure Eval
  */
trait Interpreters[M[_]] {

  implicit def interpreters(
    implicit
    A: MonadError[M, Throwable],
    T: Transactor[M]
  ): ExercisesApp ~> M = {
    val exerciseAndUserInterpreter: C01 ~> M = exerciseOpsInterpreter or userOpsInterpreter
    val userAndUserProgressInterpreter: C02 ~> M = userProgressOpsInterpreter or exerciseAndUserInterpreter
    val all: ExercisesApp ~> M = githubOpsInterpreter or userAndUserProgressInterpreter
    all
  }

  /** Lifts Exercise Ops to an effect capturing Monad such as Task via natural transformations
    */
  implicit def exerciseOpsInterpreter(implicit A: MonadError[M, Throwable]): ExerciseOp ~> M = new (ExerciseOp ~> M) {

    import org.scalaexercises.exercises.services.ExercisesService._

    def apply[A](fa: ExerciseOp[A]): M[A] = fa match {
      case GetLibraries()                       ⇒ A.pureEval(Eval.later(libraries))
      case GetSection(libraryName, sectionName) ⇒ A.pureEval(Eval.later(section(libraryName, sectionName)))
      case Evaluate(evalInfo)                   ⇒ A.pureEval(Eval.later(evaluate(evalInfo)))
    }
  }

  implicit def userOpsInterpreter(implicit A: MonadError[M, Throwable], T: Transactor[M], UR: UserRepository): UserOp ~> M = new (UserOp ~> M) {

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
        case GetLastSeenSection(user, library) ⇒
          UPR.getLastSeenSection(user, library).transact(T)
        case GetExerciseEvaluations(user, library, section) ⇒
          UPR.getExerciseEvaluations(user, library, section).transact(T)
        case UpdateUserProgress(userProgress) ⇒
          UPR.upsert(userProgress).transact(T)
      }
    }
  }

  implicit def githubOpsInterpreter(implicit A: MonadError[M, Throwable]): GithubOp ~> M = new (GithubOp ~> M) {

    def apply[A](fa: GithubOp[A]): M[A] = {

      object ProdGHInterpreters extends GithubInterpreters
      implicit val I: GitHub4s ~> M = ProdGHInterpreters.interpreters[M]

      fa match {
        case GetAuthorizeUrl(client_id, redirect_uri, scopes)                    ⇒ ghResponseToEntity(Github().auth.authorizeUrl(client_id, redirect_uri, scopes).exec[M])(auth ⇒ Authorize(auth.url, auth.state))
        case GetAccessToken(client_id, client_secret, code, redirect_uri, state) ⇒ ghResponseToEntity(Github().auth.getAccessToken(client_id, client_secret, code, redirect_uri, state).exec[M])(token ⇒ OAuthToken(token.access_token))
        case GetAuthUser(accessToken) ⇒ ghResponseToEntity(Github(accessToken).users.getAuth.exec[M])(user ⇒ GithubUser(
          login = user.login,
          name = user.name,
          avatar = user.avatar_url,
          url = user.html_url,
          email = user.email
        ))
        case GetRepository(owner, repo) ⇒ ghResponseToEntity(Github(sys.env.lift("GITHUB_TOKEN")).repos.get(owner, repo).exec[M])(repo ⇒
          Repository(
            subscribers = repo.status.subscribers_count,
            stargazers = repo.status.stargazers_count,
            forks = repo.status.forks_count
          ))
      }
    }

    private def ghResponseToEntity[A, B](response: M[GHResponse[A]])(f: A ⇒ B): M[B] = A.flatMap(response) {
      case Xor.Right(GHResult(result, status, headers)) ⇒ A.pure(f(result))
      case Xor.Left(e)                                  ⇒ A.raiseError[B](e)
    }

  }

}

/** Production based interpreters lifting ops to the effect capturing scalaz.concurrent.Task **/
trait ProdInterpreters extends Interpreters[Task] with TaskInstances

/** Test based interpreters lifting ops to their result identity **/
trait TestInterpreters extends Interpreters[Id] with IdInstances

object FreeExtensions {

  def scalazToCatsDisjunction[A, B](disj: A \/ B): A Xor B =
    disj.fold(l ⇒ Xor.Left(l), r ⇒ Xor.Right(r))

  implicit class FreeOps[F[_], A](f: Free[F, A]) {

    def runF[M[_]: Monad](implicit interpreter: F ~> M) = f.foldMap(interpreter)

    def runFuture(implicit interpreter: F ~> Task, T: Transactor[Task], M: Monad[Task]): Future[Throwable Xor A] = {
      val p = Promise[Throwable Xor A]
      f.foldMap(interpreter).runAsync { result: Throwable \/ A ⇒
        p.success(scalazToCatsDisjunction(result))
      }
      p.future
    }
  }

}

trait TaskInstances {
  implicit val taskMonad: MonadError[Task, Throwable] = new MonadError[Task, Throwable] {

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
  implicit val idMonad: MonadError[Id, Throwable] = new MonadError[Id, Throwable] {

    override def pure[A](x: A): Id[A] = idMonad.pure(x)

    override def ap[A, B](ff: Id[A ⇒ B])(fa: Id[A]): Id[B] = idMonad.ap(ff)(fa)

    override def map[A, B](fa: Id[A])(f: A ⇒ B): Id[B] = idMonad.map(fa)(f)

    override def flatMap[A, B](fa: Id[A])(f: A ⇒ Id[B]): Id[B] = idMonad.flatMap(fa)(f)

    override def product[A, B](fa: Id[A], fb: Id[B]): Id[(A, B)] = idMonad.product(fa, fb)

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
