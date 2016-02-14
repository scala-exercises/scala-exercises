import com.fortysevendeg.exercises.models.{ UserCreation, UserDoobieStore }
import com.fortysevendeg.exercises.persistence.domain.SaveUserProgress
import com.fortysevendeg.exercises.persistence.domain.SaveUserProgress._
import com.fortysevendeg.exercises.persistence.repositories.UserProgressDoobieRepository.{ instance ⇒ repository }
import doobie.imports._
import org.scalacheck.{ Arbitrary, Gen }
import org.scalatest._
import org.scalacheck.Shapeless._
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import test.database.TestDatabase

import scalaz.concurrent.Task

class UserProgressRepositorySpec
    extends PropSpec
    with GeneratorDrivenPropertyChecks
    with Matchers
    with BeforeAndAfterEach {

  // User Progress Creation generator
  implicitly[Arbitrary[SaveUserProgress.Request]]

  // Avoids creating strings with null chars because Postgres text fields don't support it.
  // http://stackoverflow.com/questions/1347646/postgres-error-on-insert-error-invalid-byte-sequence-for-encoding-utf8-0x0
  implicit val stringArbitrary: Arbitrary[String] = Arbitrary(Gen.identifier.map(_.replaceAll("\u0000", "")))

  val db = TestDatabase.create
  TestDatabase.update(db)

  val transactor: Transactor[Task] = TestDatabase.transactor(db)

  import transactor.yolo._

  //Satisfy User Foreign key:
  val userId = UserDoobieStore.create(new UserCreation.Request(
    login = "test" + System.currentTimeMillis(),
    name = "Test",
    githubId = "1",
    pictureUrl = "",
    githubUrl = "",
    email = "test@test.com"
  )).transact(transactor).run.toOption map (_.id) getOrElse 0l

  val userProgressGen = for {
    userProgressRequest ← Arbitrary.arbitrary[SaveUserProgress.Request]
    libraryName ← Gen.identifier
    sectionName ← Gen.identifier
    method ← Gen.identifier
    version ← Gen.choose(1, 100)
    exerciseType ← Gen.oneOf(Other, Koans)
    args ← Gen.option(stringArbitrary.arbitrary)
    succeeded ← Gen.oneOf(true, false)
  } yield userProgressRequest.copy(
    userId = userId,
    libraryName = libraryName,
    sectionName = sectionName,
    method = method,
    version = version,
    exerciseType = exerciseType,
    args = args,
    succeeded = succeeded
  )

  override def beforeEach() = repository.deleteAll().quick.run

  property("new user progress records can be created") {
    forAll(userProgressGen) { newUserProgress ⇒
      val userProgress = repository.create(newUserProgress).transact(transactor).run

      userProgress shouldBe newUserProgress.asUserProgress(userProgress.id)
    }
  }

  property("user progress can be fetched by section and exercise") {
    forAll(userProgressGen) { newUserProgress ⇒
      repository.create(newUserProgress).quick.run

      val currentUserProgress =
        repository.findByExerciseVersion(
          userId = newUserProgress.userId,
          libraryName = newUserProgress.libraryName,
          sectionName = newUserProgress.sectionName,
          method = newUserProgress.method,
          version = newUserProgress.version
        ).transact(transactor).run
      currentUserProgress.foreach(up ⇒ {
        up shouldBe newUserProgress.asUserProgress(up.id)
      })
    }
  }

  property("user progress can be deleted") {
    forAll(userProgressGen) { newUserProgress ⇒
      val userProgress = repository.create(newUserProgress).transact(transactor).run

      repository.delete(userProgress.id).quick.run
      repository.findById(userProgress.id).transact(transactor).run shouldBe empty
    }
  }

  property("all user progress records can be deleted") {
    forAll(userProgressGen) { newUserProgress ⇒
      val userProgress = repository.create(newUserProgress).transact(transactor).run

      repository.deleteAll().quick.run
      repository.findById(userProgress.id).transact(transactor).run shouldBe empty
    }
  }

  property("users progress can be queried by their ID") {
    forAll(userProgressGen) { newUserProgress ⇒
      val userProgress = repository.create(newUserProgress).transact(transactor).run

      val maybeUserProgress = repository.findById(userProgress.id).transact(transactor).run
      maybeUserProgress.fold(false)(up ⇒ {
        up == newUserProgress.asUserProgress(up.id)
      }) shouldBe true
    }
  }

  property("users progress List can be queried by the user ID") {
    forAll(userProgressGen) { newUserProgress ⇒
      val userProgress = repository.create(newUserProgress).transact(transactor).run

      val userProgressList = repository.findByUserId(userProgress.userId).transact(transactor).run

      userProgressList should not be empty
      userProgressList foreach { up ⇒
        up shouldBe newUserProgress.asUserProgress(up.id)
      }
    }
  }

  property("users progress can be updated") {
    forAll(userProgressGen) { newUserProgress ⇒
      val userProgress = repository.create(newUserProgress).transact(transactor).run
      val storedUserProgress = repository.findById(userProgress.id).transact(transactor).run

      storedUserProgress.fold(false)(u ⇒ {
        val modifiedUser = u.copy(version = u.version + 1)
        repository.update(userProgress = modifiedUser, newSucceededValue = true).quick.run

        repository.findById(u.id).transact(transactor).run.contains(modifiedUser)
      }) shouldBe true
    }
  }
}
