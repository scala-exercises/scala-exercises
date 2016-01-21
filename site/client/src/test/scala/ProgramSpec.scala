package programspec

import utest._

import actions._
import scripts.{ Program }
import factories.Factories._

object UpdateStateSpec extends TestSuite {
  def tests = TestSuite {
    'SetState{
      val newState = List(clientExercise(method = "foo", args = Seq("", "")))
      val action = SetState(newState)
      val state = List()
      assert(Program.updateState(state, action) == newState)
    }
    'UpdateExercise{
      val action = UpdateExercise("foo", Seq("one", "two"))
      val state = List(clientExercise(method = "foo", args = Seq("", "")))
      val newState = Program.updateState(state, action)
      assert(newState(0).arguments == Seq("one", "two"))
    }
  }
}
