import org.specs2.mutable.Specification
import org.specs2.specification.BeforeEach
import test.database.TestDatabase
import doobie.imports._
import scalaz.concurrent.Task
import shared.User
import com.fortysevendeg.exercises.models.{ NewUser, UserDoobieStore }

class UserStoreSpec extends Specification with BeforeEach {
  val db = TestDatabase.create
  TestDatabase.update(db)

  val transactor: Transactor[Task] = TestDatabase.transactor(db)
  import transactor.yolo._

  def before = UserDoobieStore.deleteAll.quick.run

  def newUser(
    login:      String = "47deg",
    name:       String = "47 degrees",
    githubId:   String = "47deg",
    pictureUrl: String = "http://placekitten.com/50/50",
    githubUrl:  String = "http://github.com/47deg",
    email:      String = "hi@47deg.com"
  ): NewUser = {
    NewUser(login, name, githubId, pictureUrl, githubUrl, email)
  }

  "UserDoobieStore" should {
    "create a new User and retrieve it afterwards" in {
      val aUser = newUser()
      val storedUser = UserDoobieStore.create(aUser).transact(transactor).run.get
      storedUser must beEqualTo(aUser.withId(storedUser.id))
    }

    "query users by login" in {
      val aUser = newUser()
      UserDoobieStore.create(aUser).quick.run

      val storedUser = UserDoobieStore.getByLogin(aUser.login).transact(transactor).run.get
      storedUser must beEqualTo(aUser.withId(storedUser.id))
    }

    "query users by id" in {
      val aUser = newUser()
      UserDoobieStore.create(aUser).quick.run

      val storedUser = UserDoobieStore.getByLogin(aUser.login).transact(transactor).run.get
      storedUser must beEqualTo(aUser.withId(storedUser.id))
    }

    "delete users" in {
      val aUser = newUser()
      UserDoobieStore.create(aUser).quick.run

      val storedUser = UserDoobieStore.getByLogin(aUser.login).transact(transactor).run.get
      UserDoobieStore.delete(storedUser.id).quick.run
      UserDoobieStore.getByLogin(aUser.login).transact(transactor).run must beNone
    }

    "update users" in {
      val aUser = newUser()
      UserDoobieStore.create(aUser).quick.run

      val storedUser = UserDoobieStore.getByLogin(aUser.login).transact(transactor).run.get
      val modifiedUser = storedUser.copy(email = "alice+spam@example.com")
      UserDoobieStore.update(modifiedUser).quick.run

      UserDoobieStore.getByLogin(aUser.login).transact(transactor).run.get must beEqualTo(modifiedUser)
    }
  }
}

