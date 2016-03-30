package catslib

import cats.{ Semigroup, SemigroupK }
import cats.data.NonEmptyList
import cats.data.Validated
import cats.data.Validated.{ Invalid, Valid }
import cats.data.Xor
import cats.std.list._

object ValidatedHelpers {
  case class ConnectionParams(url: String, port: Int)

  trait Read[A] {
    def read(s: String): Option[A]
  }

  object Read {
    def apply[A](implicit A: Read[A]): Read[A] = A

    implicit val stringRead: Read[String] =
      new Read[String] { def read(s: String): Option[String] = Some(s) }

    implicit val intRead: Read[Int] =
      new Read[Int] {
        def read(s: String): Option[Int] =
          if (s.matches("-?[0-9]+")) Some(s.toInt)
          else None
      }
  }

  sealed abstract class ConfigError
  final case class MissingConfig(field: String) extends ConfigError
  final case class ParseError(field: String) extends ConfigError

  import cats.data.Validated
  import cats.data.Validated.{ Invalid, Valid }

  case class Config(map: Map[String, String]) {
    def parse[A: Read](key: String): Validated[ConfigError, A] =
      map.get(key) match {
        case None ⇒ Invalid(MissingConfig(key))
        case Some(value) ⇒
          Read[A].read(value) match {
            case None    ⇒ Invalid(ParseError(key))
            case Some(a) ⇒ Valid(a)
          }
      }
  }

  import cats.Semigroup

  def parallelValidate[E: Semigroup, A, B, C](v1: Validated[E, A], v2: Validated[E, B])(f: (A, B) ⇒ C): Validated[E, C] =
    (v1, v2) match {
      case (Valid(a), Valid(b))       ⇒ Valid(f(a, b))
      case (Valid(_), i @ Invalid(_)) ⇒ i
      case (i @ Invalid(_), Valid(_)) ⇒ i
      case (Invalid(e1), Invalid(e2)) ⇒ Invalid(Semigroup[E].combine(e1, e2))
    }

  import cats.SemigroupK
  import cats.data.NonEmptyList
  import cats.std.list._

  implicit val nelSemigroup: Semigroup[NonEmptyList[ConfigError]] =
    SemigroupK[NonEmptyList].algebra[ConfigError]

  implicit val readString: Read[String] = Read.stringRead
  implicit val readInt: Read[Int] = Read.intRead

  def positive(field: String, i: Int): ConfigError Xor Int = {
    if (i >= 0) Xor.right(i)
    else Xor.left(ParseError(field))
  }
}
