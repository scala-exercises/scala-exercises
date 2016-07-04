package org.scalaexercises.client
package fp

import cats.Monad

case class IO[+A](ra: () ⇒ A) {
  def unsafePerformIO(): A = ra()
  def map[B](f: A ⇒ B): IO[B] = IO[B](() ⇒ f(unsafePerformIO()))
  def flatMap[B](f: A ⇒ IO[B]): IO[B] = {
    IO(() ⇒ f(ra()).unsafePerformIO())
  }
}

object IO extends IOFunctions with IOInstances

trait IOFunctions {
  def io[A](ra: ⇒ A) = IO(() ⇒ ra)
}

trait IOInstances {

  implicit val ioMonad = new Monad[IO] {
    override def pure[A](x: A): IO[A] = IO(() ⇒ x)
    override def flatMap[A, B](fa: IO[A])(f: (A) ⇒ IO[B]): IO[B] = fa flatMap f
  }

}

