/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

import doobie.contrib.specs2.analysisspec.AnalysisSpec
import doobie.util.query.Query
import doobie.util.update.Update
import org.specs2.mutable.Specification
import shared.UserProgress
import com.fortysevendeg.exercises.persistence.domain.{ UserProgressQueries ⇒ Q }
import test.database.DatabaseInstance

class UserProgressQueriesSpec
    extends Specification
    with AnalysisSpec
    with DatabaseInstance {

  check(Query[Unit, UserProgress](Q.all))
  check(Query[Long, UserProgress](Q.findById))
  check(Query[Long, UserProgress](Q.findByUserId))
  check(Query[Long, (String, Long, Boolean)](Q.findByUserIdAggregated))
  check(Query[(Long, String), (String, Boolean)](Q.findByLibrary))
  check(Query[(Long, String, String), UserProgress](Q.findBySection))
  check(Query[(Long, String, String, String, Int), UserProgress](Q.findByExerciseVersion))
  check(Update[(String, String, String, Int, String, String, Boolean, Long)](Q.update))
  check(Update[(Long, String, String, String, Int, String, String, Boolean)](Q.insert))
  check(Update[Long](Q.deleteById))
  check(Update[Unit](Q.deleteAll))
}
