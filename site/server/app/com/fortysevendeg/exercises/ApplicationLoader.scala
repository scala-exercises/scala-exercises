/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises

import com.fortysevendeg.exercises.controllers._
import com.fortysevendeg.exercises.utils._
import com.typesafe.config.ConfigFactory
import doobie.contrib.hikari.hikaritransactor.HikariTransactor
import doobie.util.transactor.{ DataSourceTransactor, Transactor }
import play.api.ApplicationLoader.Context
import play.api._
import play.api.db.evolutions.{ DynamicEvolutions, EvolutionsComponents }
import play.api.db.{ DBComponents, HikariCPComponents }
import play.api.libs.ws._
import play.api.libs.ws.ning.NingWSClient
import router.Routes

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scalaz.concurrent.Task
import scalaz.{ -\/, \/- }

class ExercisesApplicationLoader extends ApplicationLoader {
  def load(context: Context) = {
    val mode = context.environment.mode.toString.toLowerCase
    new Components(context.copy(
      initialConfiguration = context.initialConfiguration
      ++ Configuration(ConfigFactory.load(s"application.$mode.conf"))
    )).application
  }
}

class Components(context: Context)
    extends BuiltInComponentsFromContext(context)
    with EvolutionsComponents
    with DBComponents
    with HikariCPComponents {

  val jdbcUrl = "postgres:\\/\\/(.*):(.*)@(.*)".r

  implicit val transactor: Transactor[Task] = {
    val maybeTransactor = for {
      driver ← configuration.getString("db.default.driver")
      url ← configuration.getString("db.default.url")
      parsed = url match {
        case jdbcUrl(user, pass, newUrl) ⇒ Some((user, pass, "jdbc:postgresql://" + newUrl))
        case _                           ⇒ None
      }
      (user, pass, newUrl) ← parsed
      transactor = HikariTransactor[Task](driver, newUrl, user, pass).attemptRun match {
        case \/-(t) ⇒ Some(t)
        case -\/(e) ⇒ None
      }
    } yield transactor
    maybeTransactor.flatten getOrElse {
      DataSourceTransactor[Task](dbApi.database("default").dataSource)
    }
  }

  implicit val wsClient: WSClient = NingWSClient()

  // touch lazy val to enable
  applicationEvolutions
  override def dynamicEvolutions: DynamicEvolutions = new DynamicEvolutions

  val applicationController = new ApplicationController
  val exercisesController = new ExercisesController
  val userController = new UserController
  val oauthController = new OAuth2Controller
  val userProgressController = new UserProgressController

  val assets = new _root_.controllers.Assets(httpErrorHandler)

  val router = new Routes(
    httpErrorHandler,
    applicationController,
    userController,
    exercisesController,
    assets,
    oauthController,
    userProgressController
  )

  applicationLifecycle.addStopHook({ () ⇒
    Future(wsClient.close())
  })
}
