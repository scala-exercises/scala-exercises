/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.exercises.controllers

import play.api.http.{ ContentTypeOf, ContentTypes, Writeable }
import play.api.libs.json.JsError
import play.api.mvc.{ Codec, Request }

import scala.concurrent.ExecutionContext.Implicits.global

object `package` {

  def isAjax[A](implicit request: Request[A]) = request.headers.get("X-Requested-With").contains("XMLHttpRequest")

  implicit def contentTypeOf_Throwable(implicit codec: Codec): ContentTypeOf[Throwable] =
    ContentTypeOf[Throwable](Some(ContentTypes.TEXT))

  implicit def writeableOf_Throwable(implicit codec: Codec): Writeable[Throwable] = {
    Writeable(e ⇒ e.getMessage.getBytes("utf-8"))
  }

  implicit def contentTypeOf_JsError(implicit codec: Codec): ContentTypeOf[JsError] =
    ContentTypeOf[JsError](Some(ContentTypes.JSON))

  implicit def writeableOf_JsError(implicit codec: Codec): Writeable[JsError] = {
    Writeable(e ⇒ JsError.toJson(e).toString.getBytes("utf-8"))
  }

}
