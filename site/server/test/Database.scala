package test.database

import doobie.util.transactor.DataSourceTransactor
import play.api.db.evolutions._
import play.api.db.{ Database, Databases }
import doobie.imports._
import scalaz.concurrent.Task

object TestDatabase {
  val testDriver = "org.postgresql.Driver"
  def testUrl = "jdbc:postgresql://localhost:5432/scalaexercises_test"
  val testUsername = "scalaexercises_user"
  val testPassword = "scalaexercises_pass"

  def create: Database =
    Databases(
      driver = testDriver,
      url = testUrl,
      config = Map("user" → testUsername, "password" → testPassword)
    )

  def update(db: Database): Unit =
    Evolutions.applyEvolutions(db)

  def transactor(db: Database): Transactor[Task] =
    DataSourceTransactor[Task](db.dataSource)
}
