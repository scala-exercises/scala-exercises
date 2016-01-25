package com.fortysevendeg.exercises.services.free

import scala.language.higherKinds

import cats.data.Xor
import cats.free.Free
import cats.free.Inject
import shared.ExerciseEvaluation
import shared.Library
import shared.Section

/** Exercise Ops GADT
  */
sealed trait UserOp[A]

/** Exposes User operations as a Free monadic algebra that may be combined with other Algebras via
  * Coproduct
  */
class UserOps[F[_]](implicit I: Inject[UserOp, F]) {

}

/** Default implicit based DI factory from which instances of the UserOps may be obtained
  */
object UserOps {

  implicit def instance[F[_]](implicit I: Inject[UserOp, F]) = new UserOps[F]

}

