/*
 * Copyright 2014-2020 47 Degrees <https://47deg.com>
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

package org.scalaexercises.evaluator.types

final case class RangePosition(start: Int, point: Int, end: Int)

final case class CompilationInfo(message: String, pos: Option[RangePosition])

final case class RuntimeError(error: Throwable, position: Option[Int])

sealed trait EvalResult[+A]

object EvalResult {
  type CI = Map[String, List[CompilationInfo]]
}

import org.scalaexercises.evaluator.types.EvalResult._

final case class Exclusion(organization: String, moduleName: String)
final case class EvaluatorDependency(
    groupId: String,
    artifactId: String,
    version: String,
    exclusions: Option[List[Exclusion]] = None
)

final case class EvalRequest(
    resolvers: List[String] = Nil,
    dependencies: List[EvaluatorDependency] = Nil,
    code: String
)

final case class EvalResponse(
    msg: String,
    value: Option[String] = None,
    valueType: Option[String] = None,
    consoleOutput: Option[String] = None,
    compilationInfos: CI = Map.empty
)

object EvalResponse {

  object messages {

    val `ok`                    = "Ok"
    val `Timeout Exceded`       = "Timeout"
    val `Unresolved Dependency` = "Unresolved Dependency"
    val `Runtime Error`         = "Runtime Error"
    val `Compilation Error`     = "Compilation Error"
    val `Unforeseen Exception`  = "Unforeseen Exception"

  }

}
