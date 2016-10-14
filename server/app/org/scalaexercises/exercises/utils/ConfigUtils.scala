/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.exercises.utils

import play.api.{ Application, Play }

import scala.concurrent.duration._

object ConfigUtils {

  implicit def application: Application = Play.current

  lazy val githubAuthId = application.configuration.getString("github.client.id").getOrElse("")
  lazy val githubAuthSecret = application.configuration.getString("github.client.secret").getOrElse("")
  lazy val githubSiteOwner = application.configuration.getString("github.site.owner").getOrElse("")
  lazy val githubSiteRepo = application.configuration.getString("github.site.repo").getOrElse("")

  lazy val evaluatorUrl = application.configuration.getString("evaluator.url").getOrElse("")
  lazy val evaluatorAuthKey = application.configuration.getString("evaluator.authKey").getOrElse("")

  def callbackUrl = {
    val rootUrl = application
      .configuration
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
