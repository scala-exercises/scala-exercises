/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.exercises.services.free

import cats.free.Free
import doobie.imports._

import scalaz.concurrent.Task
import scalaz.{ -\/, \/- }

object ConnectionIOOps {

  implicit class ConnectionIOOps[A](c: ConnectionIO[A]) {
    def liftF[F[_]](implicit dbOps: DBOps[F], transactor: Transactor[Task]): Free[F, A] =
      c.transact(transactor).attemptRun match {
        case \/-(value) ⇒ dbOps.success(value)
        case -\/(e)     ⇒ dbOps.failure(e)
      }
  }

}
