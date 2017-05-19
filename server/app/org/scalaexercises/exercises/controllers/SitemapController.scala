/*
 *  scala-exercises
 *
 *  Copyright 2015-2017 47 Degrees, LLC. <http://www.47deg.com>
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
 *
 */

package org.scalaexercises.exercises.controllers

import org.scalaexercises.exercises.Secure

import java.util.UUID
import cats.free.Free

import org.scalaexercises.algebra.app._
import org.scalaexercises.algebra.exercises.ExerciseOps
import org.scalaexercises.types.exercises.{Contribution, Contributor}

import org.scalaexercises.exercises.services.ExercisesService
import org.scalaexercises.exercises.services.interpreters.ProdInterpreters
import org.scalaexercises.exercises.utils.ConfigUtils

import doobie.imports._

import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._

import scala.concurrent.Future
import scalaz.concurrent.Task

import freestyle._
import freestyle.implicits._

class SitemapController(
    implicit exerciseOps: ExerciseOps[ExercisesApp.Op],
    T: Transactor[Task]
) extends Controller
    with ProdInterpreters {

  def sitemap =
    Secure(Action.async { implicit request ⇒
      FreeS.liftPar(exerciseOps.getLibraries) map { libraries ⇒
        Ok(views.xml.templates.sitemap.sitemap(libraries = libraries))
      }
    })

}
