package exercises.example

import org.scalatest._
import shared.ExerciseRunner.{ExerciseResult, ∞}
import shared.{ExerciseRunner, Exercises}

/**
 *
 * SecondCategory prelude.
 *
 */
class SecondCategory extends FlatSpec with Matchers with Exercises {


  /**
   * When you create a case class, it automatically can be used with pattern matching since it has an extractor:
   */
  def firstExercise(res0: String): ExerciseResult[Unit] = ExerciseRunner("First Exercise") {

    println("Inside case classes args : " + res0)

    val result = "Test"

    result should be(res0)

    println("after assert")

  }(∞)

}

