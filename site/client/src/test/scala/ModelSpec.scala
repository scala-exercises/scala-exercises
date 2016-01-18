package modelspec

import utest._

import model._


object ClientExerciseSpec extends TestSuite {
  def tests = TestSuite {
    'isFilled {
      val unfilledExercise = ClientExercise("foo", Seq("partial response", ""))
      assert(!unfilledExercise.isFilled)

      val filledExercise = ClientExercise("foo", Seq("a response", "another response"))
      assert(filledExercise.isFilled)
    }

    'isSolved {
      val unsolvedExercise = ClientExercise("foo", Seq("foo", ""))
      assert(!unsolvedExercise.isSolved)

      val solvedExercise = ClientExercise("foo", Seq("foo"), state=Solved)
      assert(solvedExercise.isSolved)
    }

    'isBeingEvaluated {
      val unsolvedExercise = ClientExercise("foo", Seq("foo", ""))
      assert(!unsolvedExercise.isBeingEvaluated)

      val solvedExercise = ClientExercise("foo", Seq("foo"), state=Solved)
      assert(!solvedExercise.isBeingEvaluated)

      val evaluatingExercise = ClientExercise("foo", Seq("foo"), state=Evaluating)
      assert(evaluatingExercise.isBeingEvaluated)

      val erroredExercise = ClientExercise("foo", Seq("foo"), state=Errored)
      assert(!erroredExercise.isBeingEvaluated)
    }

    'canBeCompiled {
      val unfilledExercise = ClientExercise("foo", Seq("", ""))
      assert(!unfilledExercise.canBeCompiled)

      val solvedExercise = ClientExercise("foo", Seq("foo", "bar"), state=Solved)
      assert(!solvedExercise.canBeCompiled)

      val filledAndUnsolvedExercise = ClientExercise("foo", Seq("foo", "bar"), state=Unsolved)
      assert(filledAndUnsolvedExercise.canBeCompiled)

      val evaluatingExercise = ClientExercise("foo", Seq("foo", "bar"), state=Evaluating)
      assert(!evaluatingExercise.canBeCompiled)
    }
  }
}

object ExercisesSpec extends TestSuite {
  def tests = TestSuite {
    'findByMethod {
      assert(Exercises.findByMethod(List(), "foo") == None)

      val exercise = ClientExercise("foo", Seq("one", "two"))
      assert(Exercises.findByMethod(List(exercise), "foo").get == exercise)
    }

    'updateByMethod {
      val state = List(ClientExercise("foo", Seq("", "")))
      val updatedArguments = Seq("my", "answers")
      val newState = Exercises.updateByMethod(state, "foo", updatedArguments)
      val newExercise = Exercises.findByMethod(newState, "foo")
      assert(newExercise != None)
      newExercise.foreach {ex =>
        assert(ex.arguments == updatedArguments)
      }
    }
  }
}
