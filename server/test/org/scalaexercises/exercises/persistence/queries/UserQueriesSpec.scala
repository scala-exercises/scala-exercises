/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.exercises.persistence.queries

import org.scalaexercises.exercises.persistence.domain.{ UserQueries ⇒ Q }
import org.scalaexercises.exercises.persistence.repositories.UserRepository._
import org.scalaexercises.exercises.support.DatabaseInstance
import doobie.imports._
import doobie.contrib.specs2.analysisspec.AnalysisSpec
import doobie.util.query.Query
import doobie.util.update.Update
import org.specs2.mutable.Specification
import org.scalaexercises.types.user.User
import scalaz.concurrent.Task

class UserQueriesSpec
    extends Specification
    with AnalysisSpec
    with DatabaseInstance {

  implicit override val transactor: Transactor[Task] = databaseTransactor

  check(Query[Unit, User](Q.all))
  check(Query[Long, User](Q.findById))
  check(Query[String, User](Q.findByLogin))
  check(Update[UpdateParams](Q.update))
  check(Update[InsertParams](Q.insert))
  check(Update[Long](Q.deleteById))
  check(Update[Unit](Q.deleteAll))
}
