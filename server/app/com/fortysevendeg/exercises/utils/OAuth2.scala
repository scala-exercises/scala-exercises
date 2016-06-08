/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises.utils

import play.api.mvc.{ Request, AnyContent }
import play.api.{ Application, Play }
import com.fortysevendeg.exercises.controllers.OAuthController

object OAuth2 {

  implicit def application: Application = Play.current

  lazy val githubAuthId = application.configuration.getString("github.client.id").getOrElse("")
  lazy val githubAuthSecret = application.configuration.getString("github.client.secret").getOrElse("")
  lazy val githubOwner = application.configuration.getString("github.owner").getOrElse("")
  lazy val githubRepo = application.configuration.getString("github.repo").getOrElse("")
  def callbackUrl(implicit req: Request[AnyContent]) = com.fortysevendeg.exercises.controllers.routes.OAuthController.callback(None, None).absoluteURL()
  val successUrl = com.fortysevendeg.exercises.controllers.routes.OAuthController.success()

}