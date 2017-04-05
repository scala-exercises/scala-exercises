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

package org.scalaexercises.exercises.utils

import play.api.{Application, Play}

import scala.concurrent.duration._

object ConfigUtils {

  implicit def application: Application = Play.current

  lazy val githubAuthId     = getConfigString("github.client.id")
  lazy val githubAuthSecret = getConfigString("github.client.secret")
  lazy val githubSiteOwner  = getConfigString("github.site.owner")
  lazy val githubSiteRepo   = getConfigString("github.site.repo")

  lazy val evaluatorUrl     = getConfigString("evaluator.url")
  lazy val evaluatorAuthKey = getConfigString("evaluator.authKey")

  private[this] def getConfigString(key: String): String =
    application.configuration.getString(key).getOrElse("")

  def callbackUrl = {
    val rootUrl = application.configuration
      .getString("application.url")
      .getOrElse(
        throw new IllegalStateException(
          "The `application.url` setting must be present for computing the Oauth callback URL"
        )
      )
    s"$rootUrl/_oauth-callback"
  }

  val successUrl = org.scalaexercises.exercises.controllers.routes.OAuthController.success()
}
