/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises.controllers

import play.api.{ Play, Application }
import play.api.mvc._

class LoaderIOController extends Controller {

  implicit def application: Application = Play.current

  def verificationToken = Action {
    application
      .configuration
      .getString("loaderio.verificationToken", None) map (Ok(_)) getOrElse NotFound
  }

}
