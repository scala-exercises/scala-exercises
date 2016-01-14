package config

import scala.scalajs.js

object Routes {
  private val controllers = js.Dynamic.global.jsRoutes.controllers

  object Exercises {
    def libraries: String =
      controllers.ExercisesController.libraries().url.toString

    def section(libraryName: String, sectionName: String): String =
      controllers.ExercisesController.section(libraryName, sectionName).url.toString

    def evaluate(libraryName: String, sectionName: String) =
      controllers.ExercisesController.evaluate(libraryName, sectionName).url.toString
  }

}
