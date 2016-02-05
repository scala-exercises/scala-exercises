package com.fortysevendeg.exercises

import play.api._
import play.api.ApplicationLoader.Context
import play.api.db.DBComponents
import play.api.db.Database
import play.api.db.HikariCPComponents
import play.api.libs.ws._
import play.api.libs.ws.ning.NingWSClient
import play.api.routing.Router

import com.fortysevendeg.exercises.app._
import com.fortysevendeg.shared.free._
import com.fortysevendeg.exercises.services.free._
import com.fortysevendeg.exercises.services.interpreters.ProdInterpreters._
import com.fortysevendeg.exercises.controllers._
import com.fortysevendeg.exercises.utils._
import com.fortysevendeg.exercises.models.{ UserDoobieStore }

import router.Routes
import cats.Monad
import cats.free._
import scala.concurrent.ExecutionContext.Implicits.global
import scalaz.concurrent.Task
import scala.concurrent.Future
import scalaz.\/
import doobie.util.transactor.{ Transactor, DataSourceTransactor }

class ExercisesApplicationLoader() extends ApplicationLoader {
  def load(context: Context) = {
    new Components(context).application
  }
}

class Components(context: Context)
    extends BuiltInComponentsFromContext(context)
    with DBComponents
    with HikariCPComponents {

  val dataSource = dbApi.database("default").dataSource
  implicit val transactor: Transactor[Task] = DataSourceTransactor[Task](dataSource)
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

