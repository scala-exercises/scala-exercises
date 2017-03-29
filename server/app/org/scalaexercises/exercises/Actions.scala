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

package org.scalaexercises.exercises

import scala.concurrent.Future
import play.api.Play
import play.api.Play.current
import play.api.mvc._
import play.api.Logger

case class Secure[A](action: Action[A]) extends Action[A] {
  def apply(request: Request[A]): Future[Result] = {

    println("Request from domain: " + request.domain)

    val inWWW      = request.domain.startsWith("www.")
    val previewApp = request.domain.startsWith("scala-exercises-pr")

    /** Behing load balancers request.secure will be false **/
    val isSecure = request.headers
      .get("x-forwarded-proto")
      .getOrElse("")
      .contains("https") || request.secure

    val redirect =
      (!previewApp && Play.isProd && (!isSecure || !inWWW))

    if (redirect) {
      val secureUrl =
        if (inWWW) s"https://${request.domain}${request.uri}"
        else s"https://www.${request.domain}${request.uri}"

      Future.successful(
        Results
          .MovedPermanently(secureUrl)
          .withHeaders(
            "Strict-Transport-Security" → "max-age=31536000" // tells browsers to request the site URLs through HTTPS
          )
      )
    } else {
      action(request)
    }
  }

  lazy val parser = action.parser
}
