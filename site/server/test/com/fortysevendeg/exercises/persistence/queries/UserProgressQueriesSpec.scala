/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises.persistence.queries

import com.fortysevendeg.exercises.persistence.domain.{ UserProgressQueries ⇒ Q }
import com.fortysevendeg.exercises.persistence.repositories.UserProgressRepository._
import com.fortysevendeg.exercises.support.DatabaseInstance
import doobie.contrib.specs2.analysisspec.AnalysisSpec
import doobie.util.query.Query
import doobie.util.update.Update
import org.specs2.mutable.Specification
import shared.UserProgress

class UserProgressQueriesSpec
    extends Specification
    with AnalysisSpec
    with DatabaseInstance {

  check(Query[Unit, UserProgress](Q.all))
  check(Query[Long, UserProgress](Q.findById))
  check(Query[Long, UserProgress](Q.findByUserId))
  check(Query[Long, FindByUserIdAggregatedOutput](Q.findByUserIdAggregated))
  check(Query[FindByLibraryParams, FindByLibraryOutput](Q.findByLibrary))
  check(Query[FindBySectionParams, UserProgress](Q.findBySection))
  check(Query[FindByExerciseVerionParams, UserProgress](Q.findByExerciseVersion))
  check(Update[UpdateParams](Q.update))
  check(Update[InsertParams](Q.insert))
  check(Update[Long](Q.deleteById))
  check(Update[Unit](Q.deleteAll))
}
