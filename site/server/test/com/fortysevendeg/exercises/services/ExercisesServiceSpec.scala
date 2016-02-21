/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises.services

import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._

@RunWith(classOf[JUnitRunner])
class ExercisesServiceSpec extends Specification {

  val expectedLibrary = "stdlib"
  val expectedTestSection = "Extractors"
  val expectedTestExercise = "forAssigningValues"
  val expectedVersion = 1
  val expectedType = "Other"
  val expectedTestSuccesArgs = List("Chevy", "Camaro", "1978", "120")
  val expectedTestFailedArgs = List("a", "b", "1", "2")

  /*
  "ExercisesService" should {

    "return at least one library via classpath discovery" in {
      val libraries = ExercisesService.libraries
      libraries must not be empty
      libraries.find(_.description == expectedLibrary) must beSome
    }

    "return at least one category via classpath discovery" in {
      val foundSections = for {
        library ← ExercisesService.libraries
        section ← ExercisesService.section(library.description, expectedTestSection)
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
