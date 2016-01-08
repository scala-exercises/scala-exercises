package config

import scala.scalajs.js

object Routes {
  private val controllers = js.Dynamic.global.jsRoutes.controllers

  object Exercises {
    def sections: String =
      controllers.ExercisesController.sections().url.toString

    def category(section: String, category: String): String =
      controllers.ExercisesController.category(section, category).url.toString

    def evaluate(section: String, category: String) =
      controllers.ExercisesController.evaluate(section, category).url.toString
  }

}
