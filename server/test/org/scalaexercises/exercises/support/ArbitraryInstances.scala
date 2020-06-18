/*
 * Copyright 2014-2020 47 Degrees Open Source <https://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.scalaexercises.exercises.support

import cats.effect.IO
import org.scalaexercises.types.user.UserCreation._
import org.scalaexercises.types.progress.SaveUserProgress
import org.scalaexercises.exercises.persistence.repositories.UserRepository
import doobie.implicits._
import doobie._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.ScalacheckShapeless._
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.Assertions
import org.scalaexercises.types.user.User

trait ArbitraryInstances extends Assertions {

  // Avoids creating strings with null chars because Postgres text fields don't support it.
  // see http://stackoverflow.com/questions/1347646/postgres-error-on-insert-error-invalid-byte-sequence-for-encoding-utf8-0x0
  implicit val stringArbitrary: Arbitrary[String] =
    Arbitrary(Gen.identifier.map(_.replaceAll("\u0000", "")))

  implicit val userSaveRequestArbitrary: Arbitrary[Request] =
    Arbitrary(
      for {
        login      <- Gen.uuid
        name       <- Gen.alphaStr
        githubId   <- Gen.uuid
        githubUrl  <- Gen.alphaStr
        pictureUrl <- Gen.alphaStr
        email      <- Gen.alphaStr
      } yield Request(
        login = login.toString,
        name = Option(name),
        githubId = githubId.toString,
        pictureUrl = pictureUrl,
        githubUrl = githubUrl,
        email = Option(email)
      )
    )

  def persistentUserArbitrary(implicit
      transactor: Transactor[IO],
      UR: UserRepository
  ): Arbitrary[User] = {
    Arbitrary(arbitrary[Request] map { request =>
      UR.create(request).transact(transactor).unsafeRunSync match {
        case Right(user) => user
        case Left(error) => fail(s"Failed generating persistent users : $error")
      }
    })
  }

  case class UserProgressPair(request: SaveUserProgress.Request, user: User)

  implicit def saveUserProgressArbitrary(implicit
      transactor: Transactor[IO]
  ): Arbitrary[UserProgressPair] = {

    Arbitrary(for {
      user <- persistentUserArbitrary.arbitrary
      request <- {
        arbitrary[SaveUserProgress.Request] map (p => p.copy(user = user))
      }
    } yield UserProgressPair(request, user))
  }

  def genBoundedList[T](minSize: Int = 1, maxSize: Int = 100, gen: Gen[T]): Gen[List[T]] =
    Gen.choose(minSize, maxSize) flatMap { size => Gen.listOfN(size, gen) }
}

object ArbitraryInstances extends ArbitraryInstances
