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

package org.scalaexercises.exercises.services

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import org.scalaexercises.types.exercises._

class ExercisesServiceSpec extends AnyFlatSpec with Matchers {

  val expectedLibrary        = "stdlib"
  val expectedTestSection    = "Extractors"
  val expectedTestExercise   = "forAssigningValues"
  val expectedVersion        = 1
  val expectedType           = "Other"
  val expectedTestSuccesArgs = List("Chevy", "Camaro", "1978", "120")
  val expectedTestFailedArgs = List("a", "b", "1", "2")
  val resolvers = List(
    "sonatype-snapshots: https://oss.sonatype.org/content/repositories/snapshots",
    "sonatype-releases: https://oss.sonatype.org/content/repositories/releases"
  )
  val dependencies = List(
    "org.scala-lang:scala-library:2.11.7",
    "com.chuusai:shapeless:2.2.5",
    "org.scalatest:scalatest:2.2.4",
    "org.scala-exercises:exercise-compiler:0.2.3-SNAPSHOT"
  )

  def library(name: String): Library =
    Library(
      owner = "scala-exercises",
      repository = "site",
      name = name,
      description = "",
      color = "#BADA55",
      logoPath = "std_lib",
      logoData = None,
      sections = Nil,
      timestamp = "19-12-1988",
      buildInfo = BuildInfo(
        resolvers = resolvers,
        libraryDependencies = dependencies
      )
    )

  "reorderLibraries" should "not reorder libraries when there aren't any top libraries" in {
    val libraries = List(
      library("A lib"),
      library("Another lib"),
      library("Yet another lib")
    )
    val topLibraries = List()

    val reordered = new ExercisesService(this.getClass.getClassLoader)
      .reorderLibraries(topLibraries, libraries)
      .map(_.name)

    assert(reordered == libraries.map(_.name))
  }

  "reorderLibraries" should "reorder libraries when there are top libraries" in {
    val libraries = List(
      library("A lib"),
      library("Another lib"),
      library("Yet another lib")
    )
    val topLibraries = List(
      "Yet another lib",
      "A lib",
      "Another lib"
    )

    val reordered = new ExercisesService(this.getClass.getClassLoader)
      .reorderLibraries(topLibraries, libraries)
      .map(_.name)

    assert(reordered == List("Yet another lib", "A lib", "Another lib"))
  }
  /*
  "ExercisesService" should {
    "return at least one library via classpath discovery" in {
      val libraries = ExercisesService.libraries
      libraries must not be empty
      libraries.find(_.description == expectedLibrary) must beSome
    }

    "return at least one category via classpath discovery" in {
      val foundSections = for {
        library <- ExercisesService.libraries
        section <- ExercisesService.section(library.description, expectedTestSection)
      } yield section
      foundSections must not be empty
      val expectedCat = foundSections.find(_.description == expectedTestSection)
      expectedCat must beSome
      val category = expectedCat.get
      category.exercises must not be empty
      category.exercises.find(_.method.contains(expectedTestExercise)) must beSome
    }

    "evaluate a known exercise type coercing it's parameters and get a successful result" in {
      ExercisesService.evaluate(ExerciseEvaluation(
        libraryName = expectedLibrary,
        sectionName = expectedTestSection,
        method = expectedTestExercise,
        version = expectedVersion,
        exerciseType = expectedType,
        args = expectedTestSuccesArgs
      )).isRight must beTrue
    }.pendingUntilFixed("Need to update tests for new compiler")

    "evaluate a known exercise type coercing it's parameters and get a failed result" in {
      ExercisesService.evaluate(ExerciseEvaluation(
        libraryName = expectedLibrary,
        sectionName = expectedTestSection,
        method = expectedTestExercise,
        version = expectedVersion,
        exerciseType = expectedType,
        args = expectedTestFailedArgs
      )).isLeft must beTrue
    }.pendingUntilFixed("Need to update tests for new compiler")

  }
   */
}
