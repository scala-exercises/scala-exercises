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

package org.scalaexercises.exercises.persistence.repositories

import cats.effect.IO
import cats.implicits._
import org.scalaexercises.types.user.UserCreation
import org.scalaexercises.types.user.UserCreation.Request
import org.scalaexercises.exercises.support.{ArbitraryInstances, DatabaseInstance}
import doobie.implicits._
import doobie._
import org.scalacheck.Arbitrary
import org.scalacheck.ScalacheckShapeless._
import org.scalatest.matchers.should.Matchers
import org.scalatest.propspec.AnyPropSpec
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import com.dimafeng.testcontainers.ForAllTestContainer

class UserRepositorySpec
    extends AnyPropSpec
    with ScalaCheckDrivenPropertyChecks
    with Matchers
    with ArbitraryInstances
    with DatabaseInstance
    with ForAllTestContainer {

  implicit lazy val trx: Transactor[IO] = databaseTransactor

  lazy val repository: UserRepository = implicitly[UserRepository]

  // Generators
  implicitly[Arbitrary[UserCreation.Request]]

  def assertConnectionIO(cio: ConnectionIO[Boolean]): Assertion =
    assert(cio.transact(trx).unsafeRunSync())

  // Properties
  property("new users can be created") {
    forAll { newUser: Request =>
      val tx: ConnectionIO[Boolean] =
        repository.create(newUser).map { storedUser =>
          storedUser.toOption.forall(u => u == newUser.asUser(u.id))
        }
      assertConnectionIO(tx)
    }
  }

  property("users can be queried by their login") {
    forAll { newUser: Request =>
      val create = repository.create(newUser)
      val get    = repository.getByLogin(newUser.login)

      val tx: ConnectionIO[Boolean] =
        create *> get map { storedUser => storedUser.forall(u => u == newUser.asUser(u.id)) }

      assertConnectionIO(tx)
    }
  }

  property("users can be queried by their ID") {
    forAll { newUser: Request =>
      val tx: ConnectionIO[Boolean] =
        repository.create(newUser).flatMap { storedUser =>
          storedUser.toOption.fold(
            false.pure[ConnectionIO]
          )(u => repository.getById(u.id).map(_.contains(u)))
        }

      assertConnectionIO(tx)
    }
  }

  property("users can be deleted") {
    forAll { newUser: Request =>
      val tx: ConnectionIO[Boolean] =
        repository.create(newUser).flatMap { storedUser =>
          storedUser.toOption.fold(false.pure[ConnectionIO]) { u =>
            val delete = repository.delete(u.id)
            val get    = repository.getByLogin(newUser.login)
            (delete *> get).map(_.isEmpty)
          }
        }

      assertConnectionIO(tx)
    }
  }

  property("users can be updated") {
    forAll { newUser: Request =>
      val create = repository.create(newUser)
      val get    = repository.getByLogin(newUser.login)

      val tx: ConnectionIO[Boolean] =
        (create *> get).flatMap { storedUser =>
          storedUser.fold(false.pure[ConnectionIO]) { u =>
            val modifiedUser = u.copy(email = Some("alice+spam@example.com"))
            val update       = repository.update(modifiedUser)
            val get          = repository.getByLogin(u.login)
            (update *> get).map(_.contains(modifiedUser))
          }
        }

      assertConnectionIO(tx)
    }
  }
}
