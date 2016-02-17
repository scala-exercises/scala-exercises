/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

import com.fortysevendeg.exercises.models.{ UserCreation, UserDoobieStore }
import com.fortysevendeg.exercises.persistence.repositories.UserProgressRepository
import doobie.imports._
import org.scalacheck.Shapeless._
import org.scalacheck.{ Arbitrary, Gen }
import org.scalatest._
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import test.database.DatabaseInstance

class UserStoreSpec
    extends PropSpec
    with GeneratorDrivenPropertyChecks
    with Matchers
    with DatabaseInstance
    with BeforeAndAfterAll {
  // User creation generator
  implicitly[Arbitrary[UserCreation.Request]]

  // Avoids creating strings with null chars because Postgres text fields don't support it.
  // see http://stackoverflow.com/questions/1347646/postgres-error-on-insert-error-invalid-byte-sequence-for-encoding-utf8-0x0
  implicit val stringArbitrary: Arbitrary[String] =
    Arbitrary(Gen.identifier.map(_.replaceAll("\u0000", "")))

  val newUserGen = for {
    user ← Arbitrary.arbitrary[UserCreation.Request]
    login ← Gen.identifier
  } yield user.copy(login = login)

  import transactor.yolo._

  override def beforeAll() =
    implicitly[UserProgressRepository].deleteAll().transact(transactor).run

  // Properties
  property("new users can be created") {
    forAll(newUserGen) { newUser ⇒
      UserDoobieStore.deleteAll.quick.run

      val storedUser = UserDoobieStore.create(newUser).transact(transactor).run.toOption
      storedUser.fold(false)(u ⇒ {
        u == newUser.asUser(u.id)
      }) shouldBe true
    }
  }

  property("users can be queried by their login") {
    forAll(newUserGen) { newUser ⇒
      UserDoobieStore.deleteAll.quick.run

      UserDoobieStore.create(newUser).quick.run

      val storedUser = UserDoobieStore.getByLogin(newUser.login).transact(transactor).run
      storedUser.fold(false)(u ⇒ {
        u == newUser.asUser(u.id)
      }) shouldBe true
    }
  }

  property("users can be queried by their ID") {
    forAll(newUserGen) { newUser ⇒
      UserDoobieStore.deleteAll.quick.run

      val storedUser = UserDoobieStore.create(newUser).transact(transactor).run.toOption

      storedUser.fold(false)(u ⇒ {
        val userById = UserDoobieStore.getById(u.id).transact(transactor).run
        userById == Some(u)
      }) shouldBe true
    }
  }

  property("users can be deleted") {
    forAll(newUserGen) { newUser ⇒
      UserDoobieStore.deleteAll.quick.run

      val storedUser = UserDoobieStore.create(newUser).transact(transactor).run.toOption

      storedUser.fold(false)(u ⇒ {
        UserDoobieStore.delete(u.id).quick.run
        UserDoobieStore.getByLogin(newUser.login).transact(transactor).run == None
      }) shouldBe true
    }
  }

  property("users can be updated") {
    forAll(newUserGen) { newUser ⇒
      UserDoobieStore.deleteAll.quick.run

      UserDoobieStore.create(newUser).quick.run

      val storedUser = UserDoobieStore.getByLogin(newUser.login).transact(transactor).run
      storedUser.fold(false)(u ⇒ {
        val modifiedUser = u.copy(email = "alice+spam@example.com")
        UserDoobieStore.update(modifiedUser).quick.run

        UserDoobieStore.getByLogin(u.login).transact(transactor).run == Some(modifiedUser)
      }) shouldBe true
    }
  }

  property("`getOrCreate` retrieves a user if already created") {
    forAll(newUserGen) { newUser ⇒
      UserDoobieStore.deleteAll.quick.run

      UserDoobieStore.create(newUser).quick.run
      UserDoobieStore.getOrCreate(newUser).quick.run

      UserDoobieStore.all.transact(transactor).run.length shouldBe 1
    }
  }

  property("`getOrCreate` creates a user if it's not already created") {
    forAll(newUserGen) { newUser ⇒
      UserDoobieStore.deleteAll.quick.run

      UserDoobieStore.getOrCreate(newUser).quick.run

      UserDoobieStore.all.transact(transactor).run.length shouldBe 1
    }
  }
}
