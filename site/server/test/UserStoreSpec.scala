import org.specs2.mutable.Specification
import org.specs2.specification.BeforeEach
import test.database.TestDatabase
import doobie.imports._
import scalaz.concurrent.Task
import shared.User
import com.fortysevendeg.exercises.models.UserDoobieStore

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
  ): User = {
    User(None, login, name, githubId, pictureUrl, githubUrl, email)
  }

  "UserDoobieStore" should {
    "create a new User and retrieve it afterwards" in {
      val aUser = newUser()
      val storedUser = UserDoobieStore.create(
        aUser.login,
        aUser.name,
        aUser.github_id,
        aUser.picture_url,
        aUser.github_url,
        aUser.email
      ).transact(transactor).run.get

      storedUser must beEqualTo(aUser.copy(id = storedUser.id))
    }

    "query users by login" in {
      val aUser = newUser()
      UserDoobieStore.create(
        aUser.login,
        aUser.name,
        aUser.github_id,
        aUser.picture_url,
        aUser.github_url,
        aUser.email
      ).quick.run

      val storedUser = UserDoobieStore.getByLogin(aUser.login).transact(transactor).run.get
      storedUser must beEqualTo(aUser.copy(id = storedUser.id))
    }

    "query users by id" in {
      val aUser = newUser()
      UserDoobieStore.create(
        aUser.login,
        aUser.name,
        aUser.github_id,
        aUser.picture_url,
        aUser.github_url,
        aUser.email
      ).quick.run

      val storedUser = UserDoobieStore.getByLogin(aUser.login).transact(transactor).run.get
      storedUser must beEqualTo(aUser.copy(id = storedUser.id))
    }

    "delete users" in {
      val aUser = newUser()
      UserDoobieStore.create(
        aUser.login,
        aUser.name,
        aUser.github_id,
        aUser.picture_url,
        aUser.github_url,
        aUser.email
      ).quick.run

      val storedUser = UserDoobieStore.getByLogin(aUser.login).transact(transactor).run.get
      UserDoobieStore.delete(storedUser.id.get).quick.run
      UserDoobieStore.getByLogin(aUser.login).transact(transactor).run must beNone
    }

    "update users" in {
      val aUser = newUser()
      UserDoobieStore.create(
        aUser.login,
        aUser.name,
        aUser.github_id,
        aUser.picture_url,
        aUser.github_url,
        aUser.email
      ).quick.run

      val storedUser = UserDoobieStore.getByLogin(aUser.login).transact(transactor).run.get
      val modifiedUser = storedUser.copy(email = "alice+spam@example.com")
      UserDoobieStore.update(
        modifiedUser.id.get,
        modifiedUser.login,
        modifiedUser.name,
        modifiedUser.github_id,
        modifiedUser.picture_url,
        modifiedUser.github_url,
        modifiedUser.email
      ).quick.run

      UserDoobieStore.getByLogin(aUser.login).transact(transactor).run.get must beEqualTo(modifiedUser)
    }
  }
}

