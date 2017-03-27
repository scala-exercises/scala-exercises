/*
 * scala-exercises - server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
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
