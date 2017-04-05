/*
 *  scala-exercises
 *
 *  Copyright 2015-2017 47 Degrees, LLC. <http://www.47deg.com>
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
 *
 */

package org.scalaexercises.exercises.support

import doobie.util.iolite.IOLite
import doobie.util.transactor.{DriverManagerTransactor, Transactor}
import play.api.db.evolutions._
import play.api.db.{Database, Databases}

trait DatabaseInstance {
  val testDriver   = "org.postgresql.Driver"
  def testUrl      = "jdbc:postgresql://localhost:5432/scalaexercises_test"
  val testUsername = "scalaexercises_user"
  val testPassword = "scalaexercises_pass"

  def createDatabase(): Database = {
    Databases(
      driver = testDriver,
      url = testUrl,
      config = Map("user" → testUsername, "password" → testPassword)
    )
  }

  def evolve(db: Database): Database = {
    Evolutions.applyEvolutions(db)
    db
  }

  def createTransactor(db: Database): Transactor[IOLite] =
    DriverManagerTransactor[IOLite](
      testDriver,
      testUrl,
      testUsername,
      testPassword
    )

  val databaseTransactor: Transactor[IOLite] = createTransactor(evolve(createDatabase()))
}

object DatabaseInstance extends DatabaseInstance
