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

package org.scalaexercises.exercises.utils

import javax.inject.{Inject, Singleton}
import play.api.Configuration

@Singleton
case class ConfigUtils @Inject() (conf: Configuration) {

  lazy val githubAuthId: String     = getConfigString("github.client.id")
  lazy val githubAuthSecret: String = getConfigString("github.client.secret")
  lazy val githubSiteOwner: String  = getConfigString("github.site.owner")
  lazy val githubSiteRepo: String   = getConfigString("github.site.repo")

  lazy val evaluatorUrl: String     = getConfigString("evaluator.url")
  lazy val evaluatorAuthKey: String = getConfigString("evaluator.authKey")

  private[this] def getConfigString(key: String): String =
    conf.getOptional[String](key).getOrElse("")

  def callbackUrl: String = {
    val rootUrl = conf
      .getOptional[String]("application.url")
      .getOrElse(
        throw new IllegalStateException(
          "The `application.url` setting must be present for computing the Oauth callback URL"
        )
      )
    s"$rootUrl/_oauth-callback"
  }

  val successUrl = org.scalaexercises.exercises.controllers.routes.OAuthController.success
}
