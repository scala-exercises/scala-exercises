/*
 * Copyright 2014-2020 47 Degrees Open Source <https://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.scalaexercises.exercises.persistence.queries

import cats.effect.IO
import org.scalaexercises.types.progress._
import org.scalaexercises.exercises.persistence.domain.{UserProgressQueries => Q}
import org.scalaexercises.exercises.persistence.repositories.UserProgressRepository._
import org.scalaexercises.exercises.support.DatabaseInstance
import doobie.scalatest.IOChecker
import doobie.util.query.Query
import doobie.util.update.Update
import shapeless.HNil
import doobie.util.transactor.Transactor
import org.scalatest.matchers.should.Matchers
import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatestplus.scalacheck.Checkers
import com.dimafeng.testcontainers.ForAllTestContainer

class UserProgressQueriesSpec
    extends AnyFunSuiteLike
    with Matchers
    with Checkers
    with ForAllTestContainer
    with IOChecker
    with DatabaseInstance {

  import Q.Implicits._

  override def transactor: Transactor[IO] = databaseTransactor

  test("UserProgressQueries.insert")(check(Update[InsertParams](Q.insert)))
  test("UserProgressQueries.update")(check(Update[UpdateParams](Q.update)))
  test("UserProgressQueries.deleteById")(check(Update[Long](Q.deleteById)))
  test("UserProgressQueries.deleteAll")(check(Update[HNil](Q.deleteAll)))
  test("UserProgressQueries.findById")(check(Query[Long, UserProgress](Q.findById)))
  test("UserProgressQueries.findEvaluationByVersion") {
    check(Query[FindEvaluationByVersionParams, UserProgress](Q.findEvaluationByVersion))
  }
  test("UserProgressQueries.findEvaluationsBySection") {
    check(Query[FindEvaluationsBySectionParams, UserProgress](Q.findEvaluationsBySection))
  }
  test("UserProgressQueries.findLastSeenSection") {
    check(Query[FindLastSeenSectionParams, Option[String]](Q.findLastSeenSection))
  }
}
