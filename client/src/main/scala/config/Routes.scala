/*
 * Copyright 2014-2020 47 Degrees Open Source <https://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

    def progress(libraryName: String, sectionName: String): String =
      controllers.UserProgressController
        .fetchUserProgressBySection(libraryName, sectionName)
        .url
        .toString
  }

}
