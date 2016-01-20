package com.fortysevendeg.exercises

import play.api._
import play.api.ApplicationLoader.Context
import play.api.routing.Router
import com.fortysevendeg.exercises.controllers._
import router.Routes

class ExercisesApplicationLoader extends ApplicationLoader {
  def load(context: Context) = {
    new Components(context).application
  }
}

class Components(context: Context) extends BuiltInComponentsFromContext(context) {

  lazy val router = new Routes(httpErrorHandler, applicationController, exercisesController, assets)

  lazy val applicationController = new ApplicationController
  lazy val exercisesController = new ExercisesController
  lazy val assets = new _root_.controllers.Assets(httpErrorHandler)

}
