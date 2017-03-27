/*
 * scala-exercises - server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.exercises.persistence.queries

import doobie.specs2.analysisspec.AnalysisSpec
import doobie.util.iolite.IOLite
import doobie.util.query.Query
import doobie.util.transactor.Transactor
import doobie.util.update.Update
import org.scalaexercises.exercises.persistence.domain.{UserQueries => Q}
import org.scalaexercises.exercises.persistence.repositories.UserRepository._
import org.scalaexercises.exercises.support.DatabaseInstance
import org.scalaexercises.types.user.User
import org.specs2.mutable.Specification

class UserQueriesSpec extends Specification with AnalysisSpec with DatabaseInstance {

  override def transactor: Transactor[IOLite] = databaseTransactor

  check(Query[Unit, User](Q.all))
  check(Query[Long, User](Q.findById))
  check(Query[String, User](Q.findByLogin))
  check(Update[UpdateParams](Q.update))
  check(Update[InsertParams](Q.insert))
  check(Update[Long](Q.deleteById))
  check(Update[Unit](Q.deleteAll))

}
