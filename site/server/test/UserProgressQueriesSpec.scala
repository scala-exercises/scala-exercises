import doobie.contrib.specs2.analysisspec.AnalysisSpec
import doobie.util.query.Query
import org.specs2.mutable.Specification
import shared.UserProgress
import com.fortysevendeg.exercises.persistence.domain.{ UserProgressQueries ⇒ Q }
import test.database.DatabaseInstance

class UserProgressQueriesSpec extends Specification with AnalysisSpec with DatabaseInstance {

  check(Query[Unit, UserProgress](Q.all))
  check(Query[Long, UserProgress](Q.findById))

  //TODO: work in progress
}
