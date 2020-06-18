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

package org.scalaexercises.client
package modelspec

import utest._

import model._
import factories.Factories._

object ClientExerciseSpec extends TestSuite {
  def tests =
    Tests {
      "isFilled returns false when not all the responses have been filled" - {
        val filledExercise = clientExercise(args = Seq("a response", "another response"))
        assert(filledExercise.isFilled)
      }

      "isFilled returns false when not all the responses are nonempty" - {
        val unfilledExercise = clientExercise(args = Seq("partial response", "  "))
        assert(!unfilledExercise.isFilled)
      }

      "isFilled returns true when all the responses have been filled" - {
        val filledExercise = clientExercise(args = Seq("a response", "another response"))
        assert(filledExercise.isFilled)
      }

      "isSolved only returns true when the exercise state is solved" - {
        val unsolvedExercise = clientExercise(args = Seq("foo", ""))
        assert(!unsolvedExercise.isSolved)

        val solvedExercise = clientExercise(args = Seq("foo"), state = Solved)
        assert(solvedExercise.isSolved)

        val evaluatingExercise = clientExercise(args = Seq("foo"), state = Evaluating)
        assert(!evaluatingExercise.isSolved)

        val erroredExercise = clientExercise(args = Seq("foo"), state = Errored)
        assert(!erroredExercise.isSolved)
      }

      "isBeingEvaluated only returns true when the exercise is being evaluated" - {
        val unsolvedExercise = clientExercise(args = Seq("foo", ""))
        assert(!unsolvedExercise.isBeingEvaluated)

        val solvedExercise = clientExercise(args = Seq("foo"), state = Solved)
        assert(!solvedExercise.isBeingEvaluated)

        val evaluatingExercise = clientExercise(args = Seq("foo"), state = Evaluating)
        assert(evaluatingExercise.isBeingEvaluated)

        val erroredExercise = clientExercise(args = Seq("foo"), state = Errored)
        assert(!erroredExercise.isBeingEvaluated)
      }

      "canBeCompiled only returns true when an exercise is filled and not being evaluated" - {
        val unfilledExercise = clientExercise(args = Seq("", ""))
        assert(!unfilledExercise.canBeCompiled)

        val filledExercise = clientExercise(args = Seq("foo", "bar"))
        assert(filledExercise.canBeCompiled)

        val evaluatingExercise = clientExercise(args = Seq("foo", "bar"), state = Evaluating)
        assert(!evaluatingExercise.canBeCompiled)
      }
    }
}

object ExercisesSpec extends TestSuite {
  def tests =
    Tests {
      Symbol("findByMethod") {
        assert(Exercises.findByMethod(List(), "foo").isEmpty)

        val exercise = clientExercise(method = "foo", args = Seq("one", "two"))
        assert(
          Exercises.findByMethod(List(exercise), "foo").fold(false)(e => e == exercise)
        )
      }

      Symbol("updateByMethod") {
        val state            = List(clientExercise(method = "foo", args = Seq("", "")))
        val updatedArguments = Seq("my", "answers")
        val newState         = Exercises.updateByMethod(state, "foo", updatedArguments)
        val newExercise      = Exercises.findByMethod(newState, "foo")
        assert(newExercise.isDefined)
        newExercise.foreach(ex => assert(ex.arguments == updatedArguments))
      }
    }
}
