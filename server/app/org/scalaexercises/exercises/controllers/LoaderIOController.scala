/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.exercises.controllers

import org.scalaexercises.exercises.Secure
import play.api.{ Play, Application }
import play.api.mvc._

class LoaderIOController extends Controller {

  implicit def application: Application = Play.current

  def verificationToken(token: String) = Secure(Action {
    val maybeConf = application
      .configuration
      .getString("loaderio.verificationToken", None)
    maybeConf map (conf ⇒ conf == s"loaderio-$token" match {
      case true ⇒ Ok(conf)
      case _    ⇒ NotFound
    }) getOrElse NotFound
  })
}