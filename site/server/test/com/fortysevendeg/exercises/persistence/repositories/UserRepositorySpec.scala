package com.fortysevendeg.exercises.persistence.repositories

/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

import com.fortysevendeg.exercises.persistence.domain.UserCreation
import com.fortysevendeg.exercises.persistence.domain.UserCreation.Request
import com.fortysevendeg.exercises.support.{ ArbitraryInstances, DatabaseInstance }
import doobie.imports._
import org.scalacheck.Arbitrary
import org.scalacheck.Shapeless._
import org.scalatest._
import org.scalatest.prop.GeneratorDrivenPropertyChecks

class UserRepositorySpec
    extends PropSpec
    with GeneratorDrivenPropertyChecks
    with Matchers
    with DatabaseInstance
    with ArbitraryInstances
    with BeforeAndAfterAll {
  // User creation generator
  implicitly[Arbitrary[UserCreation.Request]]

  val repository = implicitly[UserRepository]

  override def beforeAll() =
    implicitly[UserProgressRepository].deleteAll().transact(transactor).run

  // Properties
  property("new users can be created") {
    forAll { newUser: Request ⇒
      repository.deleteAll().transact(transactor).run

      val storedUser = repository.create(newUser).transact(transactor).run.toOption
      storedUser.fold(false)(u ⇒ {
        u == newUser.asUser(u.id)
      }) shouldBe true
    }
  }

  property("users can be queried by their login") {
    forAll { newUser: Request ⇒
      repository.deleteAll().transact(transactor).run

      repository.create(newUser).transact(transactor).run

      val storedUser = repository.getByLogin(newUser.login).transact(transactor).run
      storedUser.fold(false)(u ⇒ {
        u == newUser.asUser(u.id)
      }) shouldBe true
    }
  }

  property("users can be queried by their ID") {
    forAll { newUser: Request ⇒
      repository.deleteAll().transact(transactor).run

      val storedUser = repository.create(newUser).transact(transactor).run.toOption

      storedUser.fold(false)(u ⇒ {
        val userById = repository.getById(u.id).transact(transactor).run
        userById.contains(u)
      }) shouldBe true
    }
  }

  property("users can be deleted") {
    forAll { newUser: Request ⇒
      repository.deleteAll().transact(transactor).run

      val storedUser = repository.create(newUser).transact(transactor).run.toOption

      storedUser.fold(false)(u ⇒ {
        repository.delete(u.id).transact(transactor).run
        repository.getByLogin(newUser.login).transact(transactor).run.isEmpty
      }) shouldBe true
    }
  }

  property("users can be updated") {
    forAll { newUser: Request ⇒
      repository.deleteAll().transact(transactor).run

      repository.create(newUser).transact(transactor).run

      val storedUser = repository.getByLogin(newUser.login).transact(transactor).run
      storedUser.fold(false)(u ⇒ {
        val modifiedUser = u.copy(email = Some("alice+spam@example.com"))
        repository.update(modifiedUser).transact(transactor).run

        repository.getByLogin(u.login).transact(transactor).run.contains(modifiedUser)
      }) shouldBe true
    }
  }

  property("`getOrCreate` retrieves a user if already created") {
    forAll { newUser: Request ⇒
      repository.deleteAll().transact(transactor).run

      repository.create(newUser).transact(transactor).run
      repository.getOrCreate(newUser).transact(transactor).run

      repository.all.transact(transactor).run.length shouldBe 1
    }
  }

  property("`getOrCreate` creates a user if it's not already created") {
    forAll { newUser: Request ⇒
      repository.deleteAll().transact(transactor).run

      repository.getOrCreate(newUser).transact(transactor).run

      repository.all.transact(transactor).run.length shouldBe 1
    }
  }
}
