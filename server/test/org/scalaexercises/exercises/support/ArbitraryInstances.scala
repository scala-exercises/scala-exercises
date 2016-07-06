/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.exercises.support

import cats.data.Xor
import org.scalaexercises.types.user.UserCreation._
import org.scalaexercises.types.progress.SaveUserProgress

import org.scalaexercises.exercises.persistence.repositories.UserRepository
import doobie.imports._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Shapeless._
import org.scalacheck.{ Arbitrary, Gen }
import org.scalatest.Assertions
import org.scalaexercises.types.user.User

import scalaz.concurrent.Task

trait ArbitraryInstances extends Assertions {

  // Avoids creating strings with null chars because Postgres text fields don't support it.
  // see http://stackoverflow.com/questions/1347646/postgres-error-on-insert-error-invalid-byte-sequence-for-encoding-utf8-0x0
  implicit val stringArbitrary: Arbitrary[String] =
    Arbitrary(Gen.identifier.map(_.replaceAll("\u0000", "")))

  implicit val userSaveRequestArbitrary: Arbitrary[Request] =
    Arbitrary(for {
      login ← Gen.uuid
      name ← Gen.alphaStr
      githubId ← Gen.uuid
      githubUrl ← Gen.alphaStr
      pictureUrl ← Gen.alphaStr
      email ← Gen.alphaStr
    } yield Request(
      login = login.toString,
      name = Option(name),
      githubId = githubId.toString,
      pictureUrl = pictureUrl,
      githubUrl = githubUrl,
      email = Option(email)
    ))

  def persistentUserArbitrary(implicit transactor: Transactor[Task], UR: UserRepository): Arbitrary[User] = {
    Arbitrary(arbitrary[Request] map { request ⇒
      UR.create(request).transact(transactor).run match {
        case Xor.Right(user) ⇒ user
        case Xor.Left(error) ⇒ fail("Failed generating persistent users : $error")
      }
    })
  }

  case class UserProgressPair(request: SaveUserProgress.Request, user: User)

  implicit def saveUserProgressArbitrary(implicit transactor: Transactor[Task]): Arbitrary[UserProgressPair] = {

    Arbitrary(for {
      user ← persistentUserArbitrary.arbitrary
      request ← {
        arbitrary[SaveUserProgress.Request] map (p ⇒ p.copy(user = user))
      }
    } yield UserProgressPair(request, user))
  }

  def genBoundedList[T](minSize: Int = 1, maxSize: Int = 100, gen: Gen[T]): Gen[List[T]] =
    Gen.choose(minSize, maxSize) flatMap { size ⇒ Gen.listOfN(size, gen) }
}

object ArbitraryInstances extends ArbitraryInstances
