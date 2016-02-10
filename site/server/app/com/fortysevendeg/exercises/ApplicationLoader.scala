package com.fortysevendeg.exercises

import doobie.contrib.hikari.hikaritransactor.HikariTransactor
import play.api._
import com.fortysevendeg.exercises.controllers._
import com.fortysevendeg.exercises.utils._
import doobie.util.transactor.{ DataSourceTransactor, Transactor }
import play.api.ApplicationLoader.Context
import play.api._
import play.api.db.{ DBComponents, HikariCPComponents }
import play.api.libs.ws._
import play.api.libs.ws.ning.NingWSClient
import router.Routes

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scalaz.{ -\/, \/-, \/ }
import doobie.util.transactor.{ DriverManagerTransactor, Transactor, DataSourceTransactor }
import scalaz.concurrent.Task

class ExercisesApplicationLoader extends ApplicationLoader {
  def load(context: Context) = {
    new Components(context).application
  }
}

class Components(context: Context)
    extends BuiltInComponentsFromContext(context)
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
      _ = Logger.warn("Parsed : " + List(user, pass, newUrl).mkString("\n"))
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

  val applicationController = new ApplicationController
  val exercisesController = new ExercisesController
  val userController = new UserController
  val oauthController = new OAuth2Controller

  val assets = new _root_.controllers.Assets(httpErrorHandler)

  val router = new Routes(httpErrorHandler, applicationController, userController, exercisesController, assets, oauthController)

  applicationLifecycle.addStopHook({ () ⇒
    Future(wsClient.close())
  })
}

