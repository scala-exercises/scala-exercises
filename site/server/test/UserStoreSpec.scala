import org.scalacheck._
import org.scalacheck.Prop.{ throws, forAll }

import org.scalacheck.Shapeless._

import test.database.TestDatabase
import doobie.imports._
import scalaz.concurrent.Task
import shared.User
import com.fortysevendeg.exercises.models.{ UserCreation, UserDoobieStore }

class UserStoreSpec extends Properties("UserDoobieStore") {
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
  property("new users can be retrieved") = forAll(newUserGen) { newUser ⇒
    UserDoobieStore.deleteAll.quick.run

    val storedUser = UserDoobieStore.create(newUser).transact(transactor).run.toOption
    storedUser.fold(false)(u ⇒ {
      u == newUser.asUser(u.id)
    })
  }

  property("users can be retrieved given their login") = forAll(newUserGen) { newUser ⇒
    UserDoobieStore.deleteAll.quick.run

    UserDoobieStore.create(newUser).quick.run

    val storedUser = UserDoobieStore.getByLogin(newUser.login).transact(transactor).run
    storedUser.fold(false)(u ⇒ {
      u == newUser.asUser(u.id)
    })
  }

  property("users can be retrieved given their ID") = forAll(newUserGen) { newUser ⇒
    UserDoobieStore.deleteAll.quick.run

    val storedUser = UserDoobieStore.create(newUser).transact(transactor).run.toOption

    storedUser.fold(false)(u ⇒ {
      val userById = UserDoobieStore.getById(u.id).transact(transactor).run
      userById == Some(u)
    })
  }

  property("users can be deleted") = forAll(newUserGen) { newUser ⇒
    UserDoobieStore.deleteAll.quick.run

    val storedUser = UserDoobieStore.create(newUser).transact(transactor).run.toOption

    storedUser.fold(false)(u ⇒ {
      UserDoobieStore.delete(u.id).quick.run
      UserDoobieStore.getByLogin(newUser.login).transact(transactor).run == None
    })
  }

  property("users can be updated") = forAll(newUserGen) { newUser ⇒
    UserDoobieStore.deleteAll.quick.run

    UserDoobieStore.create(newUser).quick.run

    val storedUser = UserDoobieStore.getByLogin(newUser.login).transact(transactor).run
    storedUser.fold(false)(u ⇒ {
      val modifiedUser = u.copy(email = "alice+spam@example.com")
      UserDoobieStore.update(modifiedUser).quick.run

      UserDoobieStore.getByLogin(u.login).transact(transactor).run == Some(modifiedUser)
    })
  }

  property("get or create users doesn't duplicate users with the same login") = forAll(newUserGen) { newUser ⇒
    UserDoobieStore.deleteAll.quick.run

    UserDoobieStore.create(newUser).quick.run
    UserDoobieStore.getOrCreate(newUser).quick.run

    UserDoobieStore.all.transact(transactor).run.length == 1
  }

  property("get or create creates the user when not found") = forAll(newUserGen) { newUser ⇒
    UserDoobieStore.deleteAll.quick.run

    UserDoobieStore.getOrCreate(newUser).quick.run

    UserDoobieStore.all.transact(transactor).run.length == 0
  }
}
