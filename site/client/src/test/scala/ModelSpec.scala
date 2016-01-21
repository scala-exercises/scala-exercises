package modelspec

import utest._

import model._
import factories.Factories._

object ClientExerciseSpec extends TestSuite {
  def tests = TestSuite {
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

    "canBeCompiled only returns true when an exercise is filled and unsolved" - {
      val unfilledExercise = clientExercise(args = Seq("", ""))
      assert(!unfilledExercise.canBeCompiled)

      val solvedExercise = clientExercise(args = Seq("foo", "bar"), state = Solved)
      assert(!solvedExercise.canBeCompiled)

      val filledAndUnsolvedExercise = clientExercise(args = Seq("foo", "bar"), state = Unsolved)
      assert(filledAndUnsolvedExercise.canBeCompiled)

      val evaluatingExercise = clientExercise(args = Seq("foo", "bar"), state = Evaluating)
      assert(!evaluatingExercise.canBeCompiled)
    }
  }
}

object ExercisesSpec extends TestSuite {
  def tests = TestSuite {
    'findByMethod{
      assert(Exercises.findByMethod(List(), "foo") == None)

      val exercise = clientExercise(method = "foo", args = Seq("one", "two"))
      assert(Exercises.findByMethod(List(exercise), "foo").get == exercise)
    }

    'updateByMethod{
      val state = List(clientExercise(method = "foo", args = Seq("", "")))
      val updatedArguments = Seq("my", "answers")
      val newState = Exercises.updateByMethod(state, "foo", updatedArguments)
      val newExercise = Exercises.findByMethod(newState, "foo")
      assert(newExercise != None)
      newExercise.foreach { ex ⇒
        assert(ex.arguments == updatedArguments)
      }
    }
  }
}
