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

package org.scalaexercises.exercises.controllers

import akka.util.ByteString
import org.scalaexercises.evaluator.types.EvalResult._
import org.scalaexercises.evaluator.types.{CompilationInfo, EvaluatorDependency}
import org.scalaexercises.types.evaluator.CoreDependency
import play.api.http.{ContentTypeOf, ContentTypes, Writeable}
import play.api.libs.json.JsError
import play.api.mvc.{Codec, Request}

object `package` {

  def isAjax[A](implicit request: Request[A]) =
    request.headers.get("X-Requested-With").contains("XMLHttpRequest")

  implicit def contentTypeOf_Throwable(implicit codec: Codec): ContentTypeOf[Throwable] =
    ContentTypeOf[Throwable](Some(ContentTypes.TEXT))

  implicit def writeableOf_Throwable(implicit codec: Codec): Writeable[Throwable] =
    Writeable(e => ByteString(e.getMessage.getBytes("utf-8")))

  implicit def contentTypeOf_JsError: ContentTypeOf[JsError] =
    ContentTypeOf[JsError](Some(ContentTypes.JSON))

  implicit def writeableOf_JsError: Writeable[JsError] =
    Writeable(e => ByteString(JsError.toJson(e).toString.getBytes("utf-8")))

  implicit class EvaluatorDependenciesConverter(deps: List[CoreDependency]) {
    def toEvaluatorDeps = deps map (d => EvaluatorDependency(d.groupId, d.artifactId, d.version))
  }

  def formatEvaluationResponse(
      msg: String,
      value: Option[String],
      valueType: Option[String],
      compilationInfos: CI
  ) = {
    val blockSeparator = ";"

    def printOption[T](maybeValue: Option[T]): String =
      maybeValue map (v => s"$blockSeparator $v") getOrElse ""

    def printTestFailed: String =
      valueType match {
        case vt if vt.contains("org.scalatest.exceptions.TestFailedException") =>
          s"$blockSeparator Compilation and evaluation succeeded but that is not the right answer!"
        case _ =>
          s"""
           |${printOption(value)}
           |${printOption(valueType)}""".stripMargin
      }

    def printCIList(list: List[CompilationInfo]): String =
      list map { ci =>
        s"${ci.message}${ci.pos.fold("")(rp => s" at [${rp.start}, ${rp.point}, ${rp.end}]")}"
      } mkString ", "

    def printCIMap: String =
      if (compilationInfos.nonEmpty)
        s"${val ciList = compilationInfos map { case (k: String, list: List[CompilationInfo]) =>
            s"""
             |$blockSeparator $k -> ${printCIList(list)}""".stripMargin
          }
          ciList.mkString(" ") }"
      else ""

    s"""$msg
       |$printTestFailed
       |$printCIMap""".stripMargin
  }
}
