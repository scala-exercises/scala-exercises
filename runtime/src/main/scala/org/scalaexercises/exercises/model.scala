/*
 *  scala-exercises
 *
 *  Copyright 2015-2017 47 Degrees, LLC. <http://www.47deg.com>
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
 *
 */

package org.scalaexercises.runtime
package model

// This is the exercise runtime metamodel

/** An exercise library.
 */
trait Library {
  def owner: String
  def repository: String
  def name: String
  def description: String
  def color: Option[String]
  def logoPath: String
  def logoData: Option[String]
  def sections: List[Section]
  def timestamp: String
  def buildMetaInfo: BuildInfo
}

/** Library Build Metadata Information
 */
trait BuildInfo {
  def resolvers: List[String]
  def libraryDependencies: List[String]
}

/** A section in a library.
 */
trait Section {
  def name: String
  def description: Option[String]
  def exercises: List[Exercise]
  def imports: List[String]
  def path: Option[String]
  def contributions: List[Contribution]
}

/** A contribution to a section.
 */
trait Contribution {
  def sha: String
  def message: String
  def timestamp: String
  def url: String
  def author: String
  def authorUrl: String
  def avatarUrl: String
}

/** Exercises within a section.
 */
trait Exercise {
  def name: String
  def description: Option[String]
  def code: String
  def qualifiedMethod: String
  def packageName: String
  def imports: List[String]
  def explanation: Option[String]
}

// default case class implementations
case class DefaultLibrary(
    owner: String,
    repository: String,
    name: String,
    description: String,
    color: Option[String],
    logoPath: String,
    logoData: Option[String],
    sections: List[Section] = Nil,
    timestamp: String,
    buildMetaInfo: BuildInfo
) extends Library

case class DefaultContribution(
    sha: String,
    message: String,
    timestamp: String,
    url: String,
    author: String,
    authorUrl: String,
    avatarUrl: String
) extends Contribution

case class DefaultSection(
    name: String,
    description: Option[String],
    exercises: List[Exercise] = Nil,
    imports: List[String] = Nil,
    path: Option[String] = None,
    contributions: List[DefaultContribution] = Nil
) extends Section

case class DefaultExercise(
    name: String,
    description: Option[String] = None,
    code: String,
    qualifiedMethod: String,
    imports: List[String],
    explanation: Option[String] = None,
    packageName: String
) extends Exercise
/*
 * scala-exercises-runtime
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */
