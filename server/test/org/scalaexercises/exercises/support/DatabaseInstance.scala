/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.exercises.support

import doobie.imports._
import play.api.db.evolutions._
import play.api.db.{ Database, Databases }

import scalaz.concurrent.Task

trait DatabaseInstance {
  val testDriver = "org.postgresql.Driver"
  def testUrl = "jdbc:postgresql://localhost:5432/scalaexercises_test"
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

  def createTransactor(db: Database): Transactor[Task] =
    DriverManagerTransactor[Task](
      testDriver,
      testUrl,
      testUsername,
      testPassword
    )

  val databaseTransactor: Transactor[Task] = createTransactor(evolve(createDatabase()))
}

object DatabaseInstance extends DatabaseInstance
