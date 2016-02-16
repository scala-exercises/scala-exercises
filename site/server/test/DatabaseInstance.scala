package test.database

import doobie.util.transactor.DataSourceTransactor
import doobie.contrib.hikari.hikaritransactor.HikariTransactor
import org.scalatest.Assertions
import play.api.db.evolutions._
import play.api.db.{ Database, Databases }
import doobie.imports._
import scalaz.{ -\/, \/- }
import scalaz.concurrent.Task

trait DatabaseInstance extends Assertions {
  val testDriver = "org.postgresql.Driver"
  def testUrl = "jdbc:postgresql://localhost:5432/scalaexercises_test"
  val testUsername = "scalaexercises_user"
  val testPassword = "scalaexercises_pass"

  def applyEvolutions(): Database = {
    val db = Databases(
      driver = testDriver,
      url = testUrl,
      config = Map("user" → testUsername, "password" → testPassword)
    )
    Evolutions.applyEvolutions(db)
    db
  }

  def createTransactor(db: Database): Transactor[Task] =
    HikariTransactor[Task](testDriver, testUrl, testUsername, testPassword).attemptRun match {
      case \/-(t) ⇒ t
      case -\/(e) ⇒ fail(s"Exception creating transactor", e)
    }

  val transactor: Transactor[Task] = createTransactor(applyEvolutions())
}

object DatabaseInstance extends DatabaseInstance
