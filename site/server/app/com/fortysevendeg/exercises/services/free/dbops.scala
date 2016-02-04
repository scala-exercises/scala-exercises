package com.fortysevendeg.exercises.services.free

import scala.language.higherKinds

import cats.free.Free
import cats.free.Inject

/** DB results GADT
  */
sealed trait DBResult[A]
final case class DBSuccess[A](a: A) extends DBResult[A]
final case class DBFailure[A](a: Throwable) extends DBResult[A]

/** Exposes DB operations as a Free monadic algebra that may be combined with other Algebras via
  * Coproduct
  */
class DBOps[F[_]](implicit I: Inject[DBResult, F]) {
  def success[A](value: A): Free[F, A] =
    Free.inject[DBResult, F](DBSuccess(value))

  def failure[A](error: Throwable): Free[F, A] =
    Free.inject[DBResult, F](DBFailure(error))
}

/** Default implicit based DI factory from which instances of the UserOps may be obtained
  */
object DBOps {

  implicit def instance[F[_]](implicit I: Inject[DBResult, F]): DBOps[F] = new DBOps[F]

}

