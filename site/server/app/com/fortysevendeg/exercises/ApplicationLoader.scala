package com.fortysevendeg.exercises

import com.fortysevendeg.exercises.models.UserSlickStore
import com.fortysevendeg.exercises.services.UserServiceImpl
import play.api._
import play.api.ApplicationLoader.Context
import play.api.db.DBComponents
import play.api.db.Database
import play.api.db.HikariCPComponents
import play.api.libs.ws.ning.NingWSClient
import play.api.routing.Router
import com.fortysevendeg.exercises.controllers._
import com.fortysevendeg.exercises.utils._
import router.Routes
import slick.jdbc.JdbcBackend
import scala.concurrent.ExecutionContext.Implicits.global

class ExercisesApplicationLoader extends ApplicationLoader {
  def load(context: Context) = {
    new Components(context).application
  }
}

class Components(context: Context)
    extends BuiltInComponentsFromContext(context)
    with DBComponents
    with HikariCPComponents {

  lazy val router = new Routes(httpErrorHandler, applicationController, userController, exercisesController, assets, OAuthController)

  lazy val applicationController = new ApplicationController(userServices)
  lazy val exercisesController = new ExercisesController
  lazy val userController = new UserController(userStore)
  lazy val OAuthController = new OAuth2Controller(NingWSClient(), userServices)
  lazy val assets = new _root_.controllers.Assets(httpErrorHandler)
  lazy val userStore = new UserSlickStore(db)
  lazy val userServices = new UserServiceImpl(userStore)
  lazy val db = JdbcBackend.Database.forDataSource(dbApi.database("default").dataSource)

}
