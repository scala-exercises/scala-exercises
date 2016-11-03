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
import org.scalaexercises.types.exercises._
import org.scalaexercises.types.exercises.ExerciseEvaluation._
import org.scalaexercises.types.progress._
import org.scalaexercises.types.user._
import org.scalaexercises.exercises.persistence.repositories.{ UserProgressRepository, UserRepository }
import org.scalaexercises.exercises.services.ExercisesService
import github4s.app.GitHub4s
import github4s.free.interpreters.{ Interpreters ⇒ GithubInterpreters, Capture ⇒ GithubCapture }
import github4s.Github
import Github._
import github4s.GithubResponses.{ GHResponse, GHResult }
import org.scalaexercises.evaluator.free.interpreters.{ Interpreter ⇒ EvaluatorInterpreter }
import org.scalaexercises.evaluator.{ Dependency ⇒ SharedDependency }
import org.scalaexercises.evaluator.EvaluatorClient._
import cats._
import cats.implicits._
import cats.free.Free
import doobie.imports._

import scala.concurrent.{ Future, Promise }
import scala.language.higherKinds
import scalaz.\/
import scalaz.concurrent.Task
import FreeExtensions._

import simulacrum.typeclass

import scala.concurrent.Future

@typeclass trait Capture[M[_]] {
  def capture[A](a: ⇒ A): M[A]
}

/** Generic interpreters that can be lazily lifted via evidence of the target F via Applicative Pure Eval
  */
trait Interpreters[M[_]] {

  /** Lifts Exercise Ops to an effect capturing Monad such as Task via natural transformations
    */
  implicit def exerciseOpsInterpreter(
    implicit
<<<<<<< HEAD
    A: MonadError[M, Throwable], C: Capture[M]
  ): ExerciseOp ~> M = λ[(ExerciseOp ~> M)] {
    case GetLibraries()                       ⇒ C.capture(ExercisesService.libraries)
    case GetSection(libraryName, sectionName) ⇒ C.capture(ExercisesService.section(libraryName, sectionName))
    case BuildRuntimeInfo(evalInfo)           ⇒ C.capture(ExercisesService.buildRuntimeInfo(evalInfo))
  }

  implicit def userOpsInterpreter(
    implicit
    A: MonadError[M, Throwable], T: Transactor[M], UR: UserRepository
  ): UserOp ~> M = λ[(UserOp ~> M)] {
    case GetUsers()            ⇒ UR.all.transact(T)
    case GetUserByLogin(login) ⇒ UR.getByLogin(login).transact(T)
    case CreateUser(newUser)   ⇒ UR.create(newUser).transact(T)
    case UpdateUser(user)      ⇒ UR.update(user).map(_.isDefined).transact(T)
    case DeleteUser(user)      ⇒ UR.delete(user.id).transact(T)
=======
    A: MonadError[M, Throwable],
    C: Capture[M]
  ): ExerciseOps.Interpreter[M] = new ExerciseOps.Interpreter[M] {

    import org.scalaexercises.exercises.services.ExercisesService._

    def getLibrariesImpl: M[List[Library]] = C.capture(libraries)

    def getSectionImpl(libraryName: String, sectionName: String): M[Option[Section]] = C.capture(section(libraryName, sectionName))

    def buildRuntimeInfoImpl(evaluation: ExerciseEvaluation): M[EvaluationRequest] = C.capture(buildRuntimeInfo(evaluation))

  }

  implicit def userOpsInterpreter(implicit A: MonadError[M, Throwable], T: Transactor[M], UR: UserRepository): UserOps.Interpreter[M] = new UserOps.Interpreter[M] {

    import UR._

    def getUsersImpl: M[List[User]] = all.transact(T)

    def getUserByLoginImpl(login: String): M[Option[User]] = getByLogin(login).transact(T)

    def createUserImpl(user: UserCreation.Request): M[UserCreation.Response] = create(user).transact(T)

    def updateUserImpl(user: User): M[Boolean] = update(user).map(_.isDefined).transact(T)

    def deleteUserImpl(user: User): M[Boolean] = delete(user.id).transact(T)
  }

  implicit def userProgressOpsInterpreter(
    implicit
    UPR: UserProgressRepository, T: Transactor[M]
  ): UserProgressOps.Interpreter[M] = new UserProgressOps.Interpreter[M] {

    def saveUserProgressImpl(userProgress: SaveUserProgress.Request): M[UserProgress] =
      UPR.upsert(userProgress).transact(T)

    def getExerciseEvaluationsImpl(user: User, library: String, section: String): M[List[UserProgress]] =
      UPR.getExerciseEvaluations(user, library, section).transact(T)

    def getLastSeenSectionImpl(user: User, library: String): M[Option[String]] =
      UPR.getLastSeenSection(user, library).transact(T)

  }

  implicit def githubOpsInterpreter(implicit A: MonadError[M, Throwable], CG: GithubCapture[M], TR: RecursiveTailRecM[M]): GithubOps.Interpreter[M] = new GithubOps.Interpreter[M] {

    implicit val I: GitHub4s ~> M = github4s.implicits.interpreters[M]

    def getAuthorizeUrlImpl(
      clientId:    String,
      redirectUri: String,
      scopes:      List[String] = List.empty
    ): M[Authorize] =
      ghResponseToEntity(Github().auth.authorizeUrl(clientId, redirectUri, scopes).exec[M])(auth ⇒ Authorize(auth.url, auth.state))

    def getAccessTokenImpl(
      clientId:     String,
      clientSecret: String,
      code:         String,
      redirectUri:  String,
      state:        String
    ): M[OAuthToken] =
      ghResponseToEntity(Github().auth.getAccessToken(clientId, clientSecret, code, redirectUri, state).exec[M])(token ⇒ OAuthToken(token.access_token))

    def getAuthUserImpl(accessToken: Option[String] = None): M[GithubUser] =
      ghResponseToEntity(Github(accessToken).users.getAuth.exec[M])(user ⇒ GithubUser(
        login = user.login,
        name = user.name,
        avatar = user.avatar_url,
        url = user.html_url,
        email = user.email
      ))

    def getRepositoryImpl(owner: String, repo: String): M[Repository] =
      ghResponseToEntity(Github(sys.env.lift("GITHUB_TOKEN")).repos.get(owner, repo).exec[M])(repo ⇒
        Repository(
          subscribers = repo.status.subscribers_count,
          stargazers = repo.status.stargazers_count,
          forks = repo.status.forks_count
        ))

    private def ghResponseToEntity[A, B](response: M[GHResponse[A]])(f: A ⇒ B): M[B] = A.flatMap(response) {
      case Right(GHResult(result, status, headers)) ⇒ A.pure(f(result))
      case Left(e)                                  ⇒ A.raiseError[B](e)
    }

  }

}

/** Production based interpreters lifting ops to the effect capturing scalaz.concurrent.Task **/
trait ProdInterpreters extends Interpreters[Task] with TaskInstances {

  implicit val taskCaptureInstance = new Capture[Task] {
    override def capture[A](a: ⇒ A): Task[A] = Task.delay(a)
  }

  implicit val gitHubTaskCaptureInstance = new GithubCapture[Task] {
    override def capture[A](a: ⇒ A): Task[A] = Task.delay(a)
  }

  implicit val tailRecMTask = new RecursiveTailRecM[scalaz.concurrent.Task] {}
}

/** Test based interpreters lifting ops to their result identity **/
trait TestInterpreters extends Interpreters[Id] with IdInstances {

  implicit val idCaptureInstance = new Capture[Id] {
    override def capture[A](a: ⇒ A): Id[A] = idMonad.pure(a)
  }

  implicit val gitHubIdCaptureInstance = new GithubCapture[Id] {
    override def capture[A](a: ⇒ A): Id[A] = idMonad.pure(a)
  }

  implicit val tailRecMTask = new RecursiveTailRecM[Id] {}
}

object FreeExtensions {

  implicit class FreeOps[F[_], A](f: Free[F, A]) {
    implicit val tailRecMTask = new cats.RecursiveTailRecM[scalaz.concurrent.Task] {}

    def runFuture(implicit interpreter: F ~> Task, T: Transactor[Task], M: Monad[Task]): Future[Either[Throwable, A]] = {
      val p = Promise[Either[Throwable, A]]
      f.foldMap(interpreter).unsafePerformAsync { result: Throwable \/ A ⇒
        p.success(result.toEither)
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

    override def tailRecM[A, B](a: A)(f: A ⇒ Task[Either[A, B]]): Task[B] =
      Task.tailrecM((a: A) ⇒ f(a) map (\/.fromEither))(a)

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

    override def tailRecM[A, B](a: A)(f: A ⇒ Id[Either[A, B]]): Id[B] = defaultTailRecM(a)(f)

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
