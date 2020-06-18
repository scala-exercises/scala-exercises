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

/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

import cats.effect.IO
import org.scalaexercises.types.user.UserCreation
import org.scalaexercises.types.progress.SaveUserProgress
import org.scalaexercises.exercises.support.{ArbitraryInstances, DatabaseInstance}
import doobie._
import doobie.implicits._
import org.scalacheck.ScalacheckShapeless._
import org.scalatest.Assertion
import org.scalatest.propspec.AnyPropSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalaexercises.types.user.User
import cats.implicits._
import com.dimafeng.testcontainers.ForAllTestContainer

class UserProgressRepositorySpec
    extends AnyPropSpec
    with ScalaCheckDrivenPropertyChecks
    with Matchers
    with ArbitraryInstances
    with DatabaseInstance
    with ForAllTestContainer {

  implicit lazy val trx: Transactor[IO] = databaseTransactor

  lazy val repository: UserProgressRepository = implicitly[UserProgressRepository]
  lazy val userRepository: UserRepository     = implicitly[UserRepository]

  def assertConnectionIO(cio: ConnectionIO[Boolean]): Assertion =
    assert(cio.transact(trx).unsafeRunSync())

  def newUser(usr: UserCreation.Request): ConnectionIO[User] =
    for {
      maybeUser <- userRepository.create(usr)
    } yield maybeUser.toOption.get

  ignore("new user progress records can be created") {
    forAll(maxDiscardedFactor(10000d)) {
      (usr: UserCreation.Request, prg: SaveUserProgress.Request) =>
        val tx: ConnectionIO[Boolean] = for {
          user <- newUser(usr)
          progress = prg.copy(user = user)
          userProgress <- repository.create(progress)
        } yield userProgress == progress.asUserProgress(userProgress.id)

        assertConnectionIO(tx)
    }
  }

  ignore("existing user progress records can be updated") {
    forAll(maxDiscardedFactor(10000d)) {
      (usr: UserCreation.Request, prg: SaveUserProgress.Request, someArgs: List[String]) =>
        val tx: ConnectionIO[Boolean] = for {
          user <- newUser(usr)
          progress = prg.copy(user = user)
          _ <- repository.create(progress)
          updatedProgress = progress.copy(args = someArgs)
          userProgress <- repository.update(updatedProgress)
        } yield userProgress.equals(updatedProgress.asUserProgress(userProgress.id))

        assertConnectionIO(tx)
    }
  }

  ignore("user progress can be fetched by section") {
    forAll(maxDiscardedFactor(10000d)) {
      (usr: UserCreation.Request, prg: SaveUserProgress.Request) =>
        val tx: ConnectionIO[Boolean] = for {
          user <- newUser(usr)
          progress = prg.copy(user = user)
          userProgress <- repository.create(progress)
          currentUserProgress <- repository.getExerciseEvaluations(
            user = user,
            libraryName = prg.libraryName,
            sectionName = prg.sectionName
          )
        } yield currentUserProgress.size.equals(1) && currentUserProgress.head.equals(userProgress)

        assertConnectionIO(tx)
    }
  }

  ignore("user progress can be fetched by exercise") {
    forAll(maxDiscardedFactor(10000d)) {
      (usr: UserCreation.Request, prg: SaveUserProgress.Request) =>
        val tx: ConnectionIO[Boolean] = for {
          user <- newUser(usr)
          progress = prg.copy(user = user)
          userProgress <- repository.create(progress)
          currentUserProgress <- repository.getExerciseEvaluation(
            user = user,
            libraryName = prg.libraryName,
            sectionName = prg.sectionName,
            method = prg.method,
            version = prg.version
          )
        } yield currentUserProgress.forall(up => up.equals(userProgress))

        assertConnectionIO(tx)
    }
  }

  ignore("users progress can be queried by their ID") {
    forAll(maxDiscardedFactor(10000d)) {
      (usr: UserCreation.Request, prg: SaveUserProgress.Request) =>
        val tx: ConnectionIO[Boolean] = for {
          user <- newUser(usr)
          progress = prg.copy(user = user)
          userProgress      <- repository.create(progress)
          maybeUserProgress <- repository.findById(userProgress.id)
        } yield maybeUserProgress.forall(up => up.equals(userProgress))

        assertConnectionIO(tx)
    }

  }

  ignore("user progress can be deleted") {
    forAll(maxDiscardedFactor(10000d)) {
      (usr: UserCreation.Request, prg: SaveUserProgress.Request) =>
        val tx: ConnectionIO[Boolean] = for {
          user          <- newUser(usr)
          userProgress  <- repository.create(prg.copy(user = user))
          _             <- repository.delete(userProgress.id)
          maybeProgress <- repository.findById(userProgress.id)
        } yield maybeProgress.isEmpty

        assertConnectionIO(tx)
    }
  }

  ignore("all user progress records can be deleted") {
    forAll(maxDiscardedFactor(10000d)) {
      (usr: UserCreation.Request, prg: SaveUserProgress.Request) =>
        val tx: ConnectionIO[Boolean] = for {
          user          <- newUser(usr)
          userProgress  <- repository.create(prg.copy(user = user))
          _             <- repository.deleteAll()
          maybeProgress <- repository.findById(userProgress.id)
        } yield maybeProgress.isEmpty

        assertConnectionIO(tx)
    }
  }
}
