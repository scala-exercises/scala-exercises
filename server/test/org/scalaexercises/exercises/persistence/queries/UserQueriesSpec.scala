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
import doobie.scalatest.IOChecker
import doobie.util.query.Query
import doobie.util.transactor.Transactor
import doobie.util.update.Update
import org.scalaexercises.exercises.persistence.domain.{UserQueries => Q}
import org.scalaexercises.exercises.persistence.repositories.UserRepository._
import org.scalaexercises.exercises.support.DatabaseInstance
import org.scalaexercises.types.user.User
import org.scalatest.matchers.should.Matchers
import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatestplus.scalacheck.Checkers
import com.dimafeng.testcontainers.ForAllTestContainer

class UserQueriesSpec
    extends AnyFunSuiteLike
    with Matchers
    with Checkers
    with ForAllTestContainer
    with IOChecker
    with DatabaseInstance {

  override def transactor: Transactor[IO] = databaseTransactor

  test("UserQueries.all")(check(Query[Unit, User](Q.all)))
  test("UserQueries.findById")(check(Query[Long, User](Q.findById)))
  test("UserQueries.findByLogin")(check(Query[String, User](Q.findByLogin)))
  test("UserQueries.update")(check(Update[UpdateParams](Q.update)))
  test("UserQueries.insert")(check(Update[InsertParams](Q.insert)))
  test("UserQueries.deleteById")(check(Update[Long](Q.deleteById)))
  test("UserQueries.deleteAll")(check(Update[Unit](Q.deleteAll)))

}
