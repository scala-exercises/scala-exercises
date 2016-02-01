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

  def db: Database = {
    val db = Databases(
      driver = testDriver,
      url = testUrl,
      config = Map("user" → testUsername, "password" → testPassword)
    )
    Evolutions.applyEvolutions(db)
    db
  }

  def dataSource: DataSource = db.dataSource

  def transactor: Transactor[Task] = DataSourceTransactor[Task](dataSource)
}
