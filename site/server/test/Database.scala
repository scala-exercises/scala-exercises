package test.database

import java.io.File
import javax.sql.DataSource
import play.api.db.evolutions._
import play.api.db.{ Database, Databases }
import doobie.imports._
import scalaz.concurrent.Task

object TestDatabase {
  val testDriver = "org.h2.Driver"
  def testUrl = {
    val dbFile = File.createTempFile("temporary-db", ".tmp").getAbsolutePath()
    s"jdbc:h2:file:$dbFile;DATABASE_TO_UPPER=false"
  }
  val testUsername = "sa"
  val testPassword = ""

  def create: Database = {
    Databases(
      driver = testDriver,
      url = testUrl,
      config = Map("user" → testUsername, "password" → testPassword)
    )
  }

  def update(db: Database): Unit =
    Evolutions.applyEvolutions(db)

  def transactor(db: Database): Transactor[Task] =
    DataSourceTransactor[Task](db.dataSource)
}
