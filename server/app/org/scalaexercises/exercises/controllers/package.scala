/*
 * scala-exercises - server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.exercises.controllers

import org.scalaexercises.evaluator.CompilationInfo
import org.scalaexercises.evaluator.EvalResult._
import play.api.http.{ContentTypeOf, ContentTypes, Writeable}
import play.api.libs.json.JsError
import play.api.mvc.{Codec, Request}
import org.scalaexercises.types.evaluator.Dependency
import org.scalaexercises.evaluator.{Dependency ⇒ EvaluatorDependency}

import scala.concurrent.ExecutionContext.Implicits.global

import org.scalaexercises.exercises.utils._

import org.scalaexercises.evaluator.EvaluatorClient
import org.scalaexercises.evaluator.EvaluatorClient._

object `package` {

  lazy val evaluatorClient: EvaluatorClient = new EvaluatorClient(
    ConfigUtils.evaluatorUrl,
    ConfigUtils.evaluatorAuthKey
  )

  def isAjax[A](implicit request: Request[A]) =
    request.headers.get("X-Requested-With").contains("XMLHttpRequest")

  implicit def contentTypeOf_Throwable(implicit codec: Codec): ContentTypeOf[Throwable] =
    ContentTypeOf[Throwable](Some(ContentTypes.TEXT))

  implicit def writeableOf_Throwable(implicit codec: Codec): Writeable[Throwable] =
    Writeable(e ⇒ e.getMessage.getBytes("utf-8"))

  implicit def contentTypeOf_JsError(implicit codec: Codec): ContentTypeOf[JsError] =
    ContentTypeOf[JsError](Some(ContentTypes.JSON))

  implicit def writeableOf_JsError(implicit codec: Codec): Writeable[JsError] =
    Writeable(e ⇒ JsError.toJson(e).toString.getBytes("utf-8"))

  implicit class EvaluatorDependenciesConverter(deps: List[Dependency]) {
    def toEvaluatorDeps = deps map (d ⇒ EvaluatorDependency(d.groupId, d.artifactId, d.version))
  }

  def formatEvaluationResponse(
      msg: String,
      value: Option[String],
      valueType: Option[String],
      compilationInfos: CI
  ) = {
    val blockSeparator = ";"

    def printOption[T](maybeValue: Option[T]): String =
      maybeValue map (v ⇒ s"$blockSeparator $v") getOrElse ""

    def printTestFailed: String = valueType match {
      case vt if vt.contains("org.scalatest.exceptions.TestFailedException") ⇒
        s"$blockSeparator Compilation and evaluation succeeded but that is not the right answer!"
      case _ ⇒
        s"""
           |${printOption(value)}
           |${printOption(valueType)}""".stripMargin
    }

    def printCIList(list: List[CompilationInfo]): String =
      list map { ci ⇒
        s"${ci.message}${ci.pos.fold("")(rp ⇒ s" at [${rp.start}, ${rp.point}, ${rp.end}]")}"
      } mkString ", "

    def printCIMap: String =
      if (compilationInfos.nonEmpty)
        s"${
          val ciList = compilationInfos map {
            case (k: String, list: List[CompilationInfo]) ⇒
              s"""
             |$blockSeparator $k -> ${printCIList(list)}""".stripMargin
          }
          ciList.mkString(" ")
        }"
      else ""

    s"""$msg
       |$printTestFailed
       |$printCIMap""".stripMargin
  }
}
