/*
 * scala-exercises-client
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.client
package config

import scala.scalajs.js

object Routes {
  private val controllers = js.Dynamic.global.jsRoutes.org.scalaexercises.exercises.controllers

  object Exercises {
    def libraries: String =
      controllers.ExercisesController.libraries().url.toString

    def section(libraryName: String, sectionName: String): String =
      controllers.ExercisesController.section(libraryName, sectionName).url.toString

    def evaluate(libraryName: String, sectionName: String): String =
      controllers.ExercisesController.evaluate(libraryName, sectionName).url.toString

    def progress(libraryName: String, sectionName: String): String = {
      controllers.UserProgressController.fetchUserProgressBySection(libraryName, sectionName).url.toString
    }
  }

}
