package com.fortysevendeg.exercises.services.free

import scala.language.higherKinds

import cats.data.Xor
import cats.free.Free
import cats.free.Inject

/** DB Ops GADT
  */
sealed trait DBOp[A]

/** Exposes DB operations as a Free monadic algebra that may be combined with other Algebras via
  * Coproduct
  */
class DBOps[F[_]](implicit I: Inject[DBOp, F]) {

}

/** Default implicit based DI factory from which instances of the DBOps may be obtained
  */
object DBOps {

  implicit def instance[F[_]](implicit I: Inject[DBOp, F]) = new DBOps[F]

}

