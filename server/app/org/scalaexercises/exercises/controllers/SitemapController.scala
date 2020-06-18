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

package org.scalaexercises.exercises.controllers

import cats.effect.IO
import org.scalaexercises.algebra.exercises.ExerciseOps
import org.scalaexercises.exercises.Secure
import play.api.Mode
import play.api.mvc._

class SitemapController(components: ControllerComponents)(implicit
    exerciseOps: ExerciseOps[IO],
    mode: Mode
) extends BaseController {

  def sitemap =
    Secure(Action.async { _ =>
      (exerciseOps.getLibraries map { libraries =>
        Ok(views.xml.templates.sitemap.sitemap(libraries))
      }).unsafeToFuture()
    })

  override protected def controllerComponents: ControllerComponents = components
}
