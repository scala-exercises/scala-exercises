/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises.persistence.queries

import com.fortysevendeg.exercises.persistence.domain.{ UserProgressQueries ⇒ Q, UserProgress }
import com.fortysevendeg.exercises.persistence.repositories.UserProgressRepository._
import com.fortysevendeg.exercises.support.DatabaseInstance
import doobie.contrib.specs2.analysisspec.AnalysisSpec
import doobie.imports._
import doobie.util.query.Query
import doobie.util.update.Update
import org.specs2.mutable.Specification
import shapeless.HNil
import doobie.contrib.postgresql.pgtypes._
import scalaz.concurrent.Task

class UserProgressQueriesSpec
    extends Specification
    with AnalysisSpec
    with DatabaseInstance {

  import Q.Implicits._

  implicit override val transactor: Transactor[Task] = databaseTransactor

  check(Update[InsertParams](Q.insert))
  check(Update[UpdateParams](Q.update))
  check(Update[Long](Q.deleteById))
  check(Update[HNil](Q.deleteAll))
  check(Query[Long, UserProgress](Q.findById))
  check(Query[FindEvaluationByVersionParams, UserProgress](Q.findEvaluationByVersion))
  check(Query[FindEvaluationsBySectionParams, UserProgress](Q.findEvaluationsBySection))
}
