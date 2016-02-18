/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */
import com.fortysevendeg.exercises.persistence.repositories.UserProgressRepository
import doobie.imports._
import org.scalatest._
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import test.database.DatabaseInstance
import org.scalacheck.Shapeless._

import scalaz.concurrent.Task

class UserProgressRepositorySpec
    extends PropSpec
    with GeneratorDrivenPropertyChecks
    with Matchers
    with ArbitraryInstances
    with DatabaseInstance {

  implicit val trx: Transactor[Task] = transactor
  val repository = implicitly[UserProgressRepository]

  property("new user progress records can be created") {
    forAll { pair: UserProgressPair ⇒
      val userProgress = repository.create(pair.request).transact(transactor).run

      userProgress shouldBe pair.request.asUserProgress(userProgress.id)
    }
  }

  property("user progress can be fetched by section") {
    forAll { pair: UserProgressPair ⇒
      repository.create(pair.request).transact(transactor).run

      val currentUserProgress =
        repository.findBySection(
          userId = pair.user.id,
          libraryName = pair.request.libraryName,
          sectionName = pair.request.sectionName
        ).transact(transactor).run
      currentUserProgress.foreach(up ⇒ {
        up shouldBe pair.request.asUserProgress(up.id)
      })
    }
  }

  property("user progress can be fetched by section, grouping the exercises in the section") {
    forAll { pair: UserProgressPair ⇒
      repository.create(pair.request).transact(transactor).run

      val userProgressBySection =
        repository.findUserProgressBySection(
          user = pair.user,
          libraryName = pair.request.libraryName,
          sectionName = pair.request.sectionName
        ).transact(transactor).run

      userProgressBySection.succeeded shouldBe pair.request.succeeded
    }
  }

  property("user progress can be fetched by library") {
    forAll { pair: UserProgressPair ⇒
      repository.create(pair.request).transact(transactor).run

      val upByLibrary =
        repository.findByLibrary(
          userId = pair.user.id,
          libraryName = pair.request.libraryName
        ).transact(transactor).run

      upByLibrary.size shouldBe 1
      val (_, succeeded) = upByLibrary.head
      succeeded shouldBe pair.request.succeeded
    }
  }

  property("user progress can be fetched by library, grouping by the succeeded field") {
    forAll { pair: UserProgressPair ⇒
      repository.create(pair.request).transact(transactor).run

      val upByLibrary =
        repository.findUserProgressByLibrary(
          user = pair.user,
          libraryName = pair.request.libraryName
        ).transact(transactor).run

      upByLibrary.size shouldBe 1
      upByLibrary.head.succeeded shouldBe pair.request.succeeded
    }
  }

  property("user progress can be fetched by section and exercise") {
    forAll { pair: UserProgressPair ⇒
      repository.create(pair.request).transact(transactor).run

      val currentUserProgress =
        repository.findByExerciseVersion(
          userId = pair.user.id,
          libraryName = pair.request.libraryName,
          sectionName = pair.request.sectionName,
          method = pair.request.method,
          version = pair.request.version
        ).transact(transactor).run
      currentUserProgress.foreach(up ⇒ {
        up shouldBe pair.request.asUserProgress(up.id)
      })
    }
  }

  property("user progress can be deleted") {
    forAll { pair: UserProgressPair ⇒

      val userProgress = repository.create(pair.request).transact(transactor).run

      repository.delete(userProgress.id).transact(transactor).run
      repository.findById(userProgress.id).transact(transactor).run shouldBe empty
    }
  }

  property("all user progress records can be deleted") {
    forAll { pair: UserProgressPair ⇒
      val userProgress = repository.create(pair.request).transact(transactor).run

      repository.deleteAll().transact(transactor).run
      repository.findById(userProgress.id).transact(transactor).run shouldBe empty
    }
  }

  property("users progress can be queried by their ID") {
    forAll { pair: UserProgressPair ⇒
      val userProgress = repository.create(pair.request).transact(transactor).run

      val maybeUserProgress = repository.findById(userProgress.id).transact(transactor).run
      maybeUserProgress.fold(false)(up ⇒ {
        up == pair.request.asUserProgress(up.id)
      }) shouldBe true
    }
  }

  property("users progress List can be queried by the user ID") {
    forAll { pair: UserProgressPair ⇒
      val userProgress = repository.create(pair.request).transact(transactor).run

      val userProgressList = repository.findByUserId(userProgress.userId).transact(transactor).run

      userProgressList should not be empty
      userProgressList foreach { up ⇒
        up shouldBe pair.request.asUserProgress(up.id)
      }
    }
  }

  property("users progress can be updated") {
    forAll { pair: UserProgressPair ⇒

      val userProgress = repository.create(pair.request).transact(transactor).run
      val modifiedUserProgress = userProgress.copy(version = userProgress.version + 1)
      repository.update(userProgress = modifiedUserProgress).transact(transactor).run
      val fetchedProgress = repository.findById(userProgress.id).transact(transactor).run

      fetchedProgress foreach { up ⇒
        up.version shouldBe userProgress.version + 1
        up.id shouldBe userProgress.id
        up.userId shouldBe userProgress.userId
      }
    }
  }
}
