/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
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

    val inWWW = request.domain.startsWith("www.")
    val previewApp = request.domain.startsWith("scala-exercises-pr")
    /** Behing load balancers request.secure will be false **/
    val isSecure = request.headers.get("x-forwarded-proto").getOrElse("").contains("https") || request.secure

    val redirect =
      (!previewApp && Play.isProd && (!isSecure || !inWWW))

    if (redirect) {
      val secureUrl =
        if (inWWW) s"https://${request.domain}${request.uri}"
        else s"https://www.${request.domain}${request.uri}"

      Future.successful(
        Results.MovedPermanently(secureUrl).withHeaders(
          "Strict-Transport-Security" → "max-age=31536000" // tells browsers to request the site URLs through HTTPS
        )
      )
    } else {
      action(request)
    }
  }

  lazy val parser = action.parser
}
