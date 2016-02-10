import org.scalatest._
import prop._
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalacheck.{ Gen, Arbitrary }
import org.scalacheck.Shapeless._

import test.database.TestDatabase
import doobie.imports._
import scalaz.concurrent.Task
import shared.User
import com.fortysevendeg.exercises.models.{ UserCreation, UserDoobieStore }

class UserStoreSpec extends PropSpec with GeneratorDrivenPropertyChecks with Matchers {
  // User creation generator
  implicitly[Arbitrary[UserCreation.Request]]

  val newUserGen = for {
    user ← Arbitrary.arbitrary[UserCreation.Request]
    login ← Gen.identifier
  } yield user.copy(login = login)

  // Test database setup
  val db = TestDatabase.create
  TestDatabase.update(db)

  val transactor: Transactor[Task] = TestDatabase.transactor(db)
  import transactor.yolo._

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
