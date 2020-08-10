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

package org.scalaexercises.types.exercises

import org.scalaexercises.types.evaluator.CoreDependency

/**
 * A library representing a lib or lang. Ej. stdlib, cats, scalaz...
 */
case class Library(
    owner: String,
    repository: String,
    name: String,
    description: String,
    color: String,
    logoPath: String,
    logoData: Option[String],
    sections: List[Section] = Nil,
    timestamp: String,
    buildInfo: BuildInfo
) {
  val sectionNames: List[String] = sections map (_.name)
  val shortTimestamp: String     = timestamp.split('T').headOption.getOrElse(timestamp)
}

/**
 * Represents the Library Build Metadata Information
 */
case class BuildInfo(
    resolvers: List[String],
    libraryDependencies: List[String]
)

/**
 * A section in a library. For example `Extractors`
 */
case class Section(
    name: String,
    description: Option[String] = None,
    path: Option[String] = None,
    exercises: List[Exercise] = Nil,
    contributions: List[Contribution] = Nil
)

/**
 * Exercises within a Category
 */
case class Exercise(
    method: String,
    name: Option[String] = None,
    description: Option[String] = None,
    code: Option[String] = None,
    explanation: Option[String] = None
)

/**
 * Input params necessary to evaluate an exercise
 */
case class ExerciseEvaluation(
    libraryName: String,
    sectionName: String,
    method: String,
    version: Int,
    exerciseType: ExerciseType,
    args: List[String]
)

object ExerciseEvaluation {
  type EvaluationRequest = Either[String, (List[String], List[CoreDependency], String)]
  type Result            = Either[String, Any]
}

sealed abstract class ExerciseType extends Product with Serializable
case object Koans                  extends ExerciseType
case object Other                  extends ExerciseType

object ExerciseType {
  def fromString(s: String): ExerciseType =
    Vector(Koans, Other).find(ex => ex.toString == s) getOrElse Other

  def toString(e: ExerciseType): String = e.toString
}

case class Contribution(
    sha: String,
    message: String,
    timestamp: String,
    url: String,
    author: String,
    authorUrl: String,
    avatarUrl: String
)

case class Contributor(
    author: String,
    authorUrl: String,
    avatarUrl: String
)
