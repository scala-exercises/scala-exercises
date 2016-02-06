package com.fortysevendeg.exercises.services.free

import com.fortysevendeg.exercises.persistence.domain.SaveUserProgress
import shared.UserProgress

import scala.language.higherKinds

import cats.free.Free
import cats.free.Inject

/** Users Progress Ops GADT
  */
sealed trait UserProgressOp[A]
final case class UpdateUserProgress(userProgress: SaveUserProgress.Request) extends UserProgressOp[UserProgress]

/** Exposes User Progress operations as a Free monadic algebra that may be combined with other Algebras via
  * Coproduct
  */
class UserProgressOps[F[_]](implicit I: Inject[UserProgressOp, F]) {

  def saveUserProgress(userProgress: SaveUserProgress.Request): Free[F, UserProgress] =
    Free.inject[UserProgressOp, F](UpdateUserProgress(userProgress))
}

/** Default implicit based DI factory from which instances of the UserOps may be obtained
  */
object UserProgressOps {

  implicit def instance[F[_]](implicit I: Inject[UserProgressOp, F]): UserProgressOps[F] = new UserProgressOps[F]

}

