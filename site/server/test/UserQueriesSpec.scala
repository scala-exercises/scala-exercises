import doobie.imports._

import play.api.db.evolutions._
import play.api.db.{ Database, Databases }
import shared.User
import doobie.contrib.specs2.analysisspec.AnalysisSpec
import scalaz.concurrent.Task
import com.fortysevendeg.exercises.models.queries.Queries
import org.specs2.mutable.Specification

class UserQueriesSpec extends Specification with AnalysisSpec {
  val db =
    Databases(
      driver = "org.h2.Driver",
      url = "jdbc:h2:file:~/.scala-exercises/test-db;DATABASE_TO_UPPER=false",
      config = Map("user" → "sa", "password" → "")
    )

  Evolutions.applyEvolutions(db)
  val transactor = DataSourceTransactor[Task](db.dataSource)

  check(Queries.all)
  check(Queries.byLogin("47deg"))
  check(Queries.byId(47))
  check(Queries.insert(47, "47deg", "47", "47deg", "http://placekitten.com/50/50", "http://github.com/47deg", "hello@47deg.com"))
  check(Queries.delete(47))
  check(Queries.update(47, "47deg", "47", "47deg", "http://placekitten.com/50/50", "http://github.com/47deg", "hello@47deg.com"))

}

