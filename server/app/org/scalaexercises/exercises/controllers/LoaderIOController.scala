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
import play.api.{Application, Play}
import play.api.mvc._

class LoaderIOController extends Controller {

  implicit def application: Application = Play.current

  def verificationToken(token: String) =
    Secure(Action {
      val maybeConf = application.configuration
        .getString("loaderio.verificationToken", None)
      maybeConf.filter(_ == s"loaderio-$token").fold[Result](NotFound)(Ok(_))
    })
}
