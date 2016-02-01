import org.specs2.mutable.Specification
import org.specs2.specification.BeforeEach
import java.io.File
import play.api.db.evolutions._
import play.api.db.{ Database, Databases }

import doobie.imports._
import shared.User
import scalaz.concurrent.Task
import com.fortysevendeg.exercises.models.UserDoobieStore

class UserStoreSpec extends Specification with BeforeEach {
  val dbFile = File.createTempFile("temporary-db", ".tmp").getAbsolutePath()

  val db =
    Databases(
      driver = "org.h2.Driver",
      url = s"jdbc:h2:file:$dbFile;DATABASE_TO_UPPER=false",
      config = Map("user" → "sa", "password" → "")
    )

  Evolutions.applyEvolutions(db)
  val transactor: Transactor[Task] = DataSourceTransactor[Task](db.dataSource)
  import transactor.yolo._

  def before = UserDoobieStore.deleteAll.quick.run

  "UserDoobieStore" should {
    "create a new User and retrieve it afterwards" in {
      val aUser = User(None, "47deg", "47 degrees", "47deg", "http://placekitten.com/50/50", "http://github.com/47deg", "hi@47deg.com")
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
      val aUser = User(None, "47deg", "47 degrees", "47deg", "http://placekitten.com/50/50", "http://github.com/47deg", "hi@47deg.com")
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
      val aUser = User(None, "47deg", "47 degrees", "47deg", "http://placekitten.com/50/50", "http://github.com/47deg", "hi@47deg.com")
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
      val aUser = User(None, "47deg", "47 degrees", "47deg", "http://placekitten.com/50/50", "http://github.com/47deg", "hi@47deg.com")
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
      val aUser = User(None, "47deg", "47 degrees", "47deg", "http://placekitten.com/50/50", "http://github.com/47deg", "hi@47deg.com")
      UserDoobieStore.create(
        aUser.login,
        aUser.name,
        aUser.github_id,
        aUser.picture_url,
        aUser.github_url,
        aUser.email
      ).quick.run

      val storedUser = UserDoobieStore.getByLogin(aUser.login).transact(transactor).run.get
      val modifiedUser = storedUser.copy(email = "hello@47deg.com")
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

