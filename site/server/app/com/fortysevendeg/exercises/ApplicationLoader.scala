package com.fortysevendeg.exercises

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

