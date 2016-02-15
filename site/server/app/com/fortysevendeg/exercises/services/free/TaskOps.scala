/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises.services.free

import cats.free.Free

import scalaz.concurrent.Task
import scalaz.{ -\/, \/- }

object TaskOps {

  implicit def liftFTask[F[_], A](t: Task[A])(implicit dbOps: DBOps[F]): Free[F, A] = t.liftF[F]

  implicit class TaskOps[A](task: Task[A]) {
    def liftF[F[_]](implicit dbOps: DBOps[F]): cats.free.Free[F, A] = task.attemptRun match {
      case \/-(value) ⇒ dbOps.success(value)
      case -\/(e)     ⇒ dbOps.failure(e)
    }
  }

}
