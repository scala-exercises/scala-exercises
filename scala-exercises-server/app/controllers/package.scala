package controllers

import play.api.http.{Writeable, ContentTypes, ContentTypeOf}
import play.api.libs.json.JsError
import play.api.mvc.{Request, Codec}
import scala.concurrent.ExecutionContext.Implicits.global

object `package` {

  def isAjax[A](implicit request : Request[A]) = {
    request.headers.get("X-Requested-With") == Some("XMLHttpRequest")
  }

  implicit def contentTypeOf_Throwable(implicit codec: Codec): ContentTypeOf[Throwable] =
    ContentTypeOf[Throwable](Some(ContentTypes.TEXT))

  implicit def writeableOf_Throwable(implicit codec: Codec): Writeable[Throwable] = {
    Writeable(e => e.getMessage.getBytes("utf-8"))
  }

  implicit def contentTypeOf_JsError(implicit codec: Codec): ContentTypeOf[JsError] =
    ContentTypeOf[JsError](Some(ContentTypes.JSON))

  implicit def writeableOf_JsError(implicit codec: Codec): Writeable[JsError] = {
    Writeable(e => JsError.toFlatJson(e).toString.getBytes("utf-8"))
  }
}