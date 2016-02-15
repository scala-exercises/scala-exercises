import doobie.contrib.specs2.analysisspec.AnalysisSpec
import doobie.imports._
import doobie.util.query.Query
import org.specs2.mutable.Specification
import shared.UserProgress
import test.database.TestDatabase
import com.fortysevendeg.exercises.persistence.domain.{ UserProgressQueries ⇒ Q }

import scalaz.concurrent.Task

class UserProgressQueriesSpec extends Specification with AnalysisSpec {
  val db = TestDatabase.create
  TestDatabase.update(db)

  val transactor: Transactor[Task] = TestDatabase.transactor(db)

  check(Query[Unit, UserProgress](Q.all))
  check(Query[Long, UserProgress](Q.findById))

  //TODO: work in progress
}
