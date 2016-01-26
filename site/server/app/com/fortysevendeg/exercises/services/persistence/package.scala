package com.fortysevendeg.exercises.services.persistence

import play.api.Play.current

import doobie.imports._
import scalaz.concurrent.Task

object persistence {
  val config = current.configuration

  val driver = config.getString("db.default.driver").getOrElse("org.h2.Driver")
  val url = config.getString("db.default.url").getOrElse("jdbc:h2:file:~/.scala-exercises/db;DATABASE_TO_UPPER=false")
  val username = config.getString("db.default.username").getOrElse("sa")
  val password = config.getString("db.default.password").getOrElse("")

  def transactor = DriverManagerTransactor[Task](
    driver,
    url,
    username,
    password
  )
}
