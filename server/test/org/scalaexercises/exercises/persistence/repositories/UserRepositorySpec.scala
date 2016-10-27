package org.scalaexercises.exercises.persistence.repositories

/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

import org.scalaexercises.types.user.UserCreation
import org.scalaexercises.types.user.UserCreation.Request

import org.scalaexercises.exercises.support.{ ArbitraryInstances, DatabaseInstance }
import doobie.imports._
import org.scalacheck.Arbitrary
import org.scalacheck.Shapeless._
import org.scalatest._
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import scalaz.concurrent.Task
import cats.implicits._

class UserRepositorySpec
    extends PropSpec
    with GeneratorDrivenPropertyChecks
    with Matchers
    with ArbitraryInstances
    with DatabaseInstance
    with BeforeAndAfterAll {

  implicit val transactor: Transactor[Task] = databaseTransactor

  val repository = implicitly[UserRepository]
  override def beforeAll() =
    repository.deleteAll.transact(transactor).unsafePerformSync

  // Generators
  implicitly[Arbitrary[UserCreation.Request]]

  // Properties
  property("new users can be created") {
    forAll { newUser: Request ⇒
      val storedUser = repository.create(newUser).transact(transactor).unsafePerformSync.toOption
      storedUser.fold(false)(u ⇒ {
        u == newUser.asUser(u.id)
      }) shouldBe true
    }
  }

  property("users can be queried by their login") {
    forAll { newUser: Request ⇒
      repository.create(newUser).transact(transactor).unsafePerformSync

      val storedUser = repository.getByLogin(newUser.login).transact(transactor).unsafePerformSync
      storedUser.fold(false)(u ⇒ {
        u == newUser.asUser(u.id)
      }) shouldBe true
    }
  }

  property("users can be queried by their ID") {
    forAll { newUser: Request ⇒
      val storedUser = repository.create(newUser).transact(transactor).unsafePerformSync.toOption

      storedUser.fold(false)(u ⇒ {
        val userById = repository.getById(u.id).transact(transactor).unsafePerformSync
        userById.contains(u)
      }) shouldBe true
    }
  }

  property("users can be deleted") {
    forAll { newUser: Request ⇒
      val storedUser = repository.create(newUser).transact(transactor).unsafePerformSync.toOption

      storedUser.fold(false)(u ⇒ {
        repository.delete(u.id).transact(transactor).unsafePerformSync
        repository.getByLogin(newUser.login).transact(transactor).unsafePerformSync.isEmpty
      }) shouldBe true
    }
  }

  property("users can be updated") {
    forAll { newUser: Request ⇒
      repository.create(newUser).transact(transactor).unsafePerformSync

      val storedUser = repository.getByLogin(newUser.login).transact(transactor).unsafePerformSync
      storedUser.fold(false)(u ⇒ {
        val modifiedUser = u.copy(email = Some("alice+spam@example.com"))
        repository.update(modifiedUser).transact(transactor).unsafePerformSync

        repository.getByLogin(u.login).transact(transactor).unsafePerformSync.contains(modifiedUser)
      }) shouldBe true
    }
  }
}
