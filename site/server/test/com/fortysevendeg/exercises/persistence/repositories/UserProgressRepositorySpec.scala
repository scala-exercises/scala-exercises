
/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises.persistence.repositories

import com.fortysevendeg.exercises.persistence.domain._

import com.fortysevendeg.exercises.support.{ ArbitraryInstances, DatabaseInstance }
import doobie.imports._
import org.scalacheck.Arbitrary
import org.scalacheck.Shapeless._
import org.scalatest._
import org.scalatest.prop.GeneratorDrivenPropertyChecks

import scalaz.concurrent.Task

class UserProgressRepositorySpec
    extends PropSpec
    with GeneratorDrivenPropertyChecks
    with Matchers
    with ArbitraryInstances
    with DatabaseInstance
    with BeforeAndAfterAll {

  implicit val trx: Transactor[Task] = transactor
  val repository = implicitly[UserProgressRepository]
  val userRepository = implicitly[UserRepository]

  // Generators
  implicitly[Arbitrary[UserCreation.Request]]
  implicitly[Arbitrary[SaveUserProgress.Request]]

  override def beforeAll() = {
    repository.deleteAll().transact(transactor).run
    userRepository.deleteAll().transact(transactor).run
  }

  property("new user progress records can be created") {
    forAll { (usr: UserCreation.Request, prg: SaveUserProgress.Request) ⇒
      val user = userRepository.create(usr).transact(transactor).run.toOption.get
      val userProgress = repository.create(prg.copy(user = user)).transact(transactor).run

      userProgress shouldBe prg.asUserProgress(userProgress.id)
    }
  }

  property("existing user progress records can be updated") {
    forAll { (usr: UserCreation.Request, prg: SaveUserProgress.Request, someArgs: List[String]) ⇒
      val user = userRepository.create(usr).transact(transactor).run.toOption.get
      val progress = prg.copy(user = user)
      val updatedProgress = progress.copy(args = someArgs)

      repository.create(progress).transact(transactor).run
      val userProgress = repository.update(updatedProgress).transact(transactor).run

      userProgress shouldBe updatedProgress.asUserProgress(user.id)
    }
  }

  property("user progress can be fetched by section") {
    forAll { (usr: UserCreation.Request, prg: SaveUserProgress.Request) ⇒
      val user = userRepository.create(usr).transact(transactor).run.toOption.get
      repository.create(prg.copy(user = user)).transact(transactor).run

      val currentUserProgress =
        repository.getExerciseEvaluations(
          user = user,
          libraryName = prg.libraryName,
          sectionName = prg.sectionName
        ).transact(transactor).run

      currentUserProgress.size shouldBe 1
      currentUserProgress.head shouldBe prg.asUserProgress(user.id)
    }
  }

  property("user progress can be fetched by exercise") {
    forAll { (usr: UserCreation.Request, prg: SaveUserProgress.Request) ⇒
      val user = userRepository.create(usr).transact(transactor).run.toOption.get
      repository.create(prg).transact(transactor).run

      val currentUserProgress =
        repository.getExerciseEvaluation(
          user = user,
          libraryName = prg.libraryName,
          sectionName = prg.sectionName,
          method = prg.method,
          version = prg.version
        ).transact(transactor).run

      currentUserProgress.isDefined shouldBe true
      currentUserProgress.get shouldBe prg.asUserProgress(user.id)
    }
  }

  property("users progress can be queried by their ID") {
    forAll { (usr: UserCreation.Request, prg: SaveUserProgress.Request) ⇒
      val user = userRepository.create(usr).transact(transactor).run.toOption.get
      val userProgress = repository.create(prg).transact(transactor).run

      val maybeUserProgress = repository.findById(userProgress.id).transact(transactor).run
      maybeUserProgress.fold(false)(up ⇒ {
        up == prg.asUserProgress(user.id)
      }) shouldBe true
    }
  }

  property("user progress can be deleted") {
    forAll { (usr: UserCreation.Request, prg: SaveUserProgress.Request) ⇒
      val user = userRepository.create(usr).transact(transactor).run.toOption.get
      val userProgress = repository.create(prg).transact(transactor).run

      repository.delete(userProgress.id).transact(transactor).run
      repository.findById(userProgress.id).transact(transactor).run shouldBe empty
    }
  }

  property("all user progress records can be deleted") {
    forAll { (usr: UserCreation.Request, prg: SaveUserProgress.Request) ⇒
      val user = userRepository.create(usr).transact(transactor).run.toOption.get
      val userProgress = repository.create(prg).transact(transactor).run

      repository.deleteAll().transact(transactor).run
      repository.findById(userProgress.id).transact(transactor).run shouldBe empty
    }
  }
}
