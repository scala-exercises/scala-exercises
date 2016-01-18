package programspec

import utest._

import model._
import actions._
import scripts.{ Program }

object UpdateStateSpec extends TestSuite {
  def tests = TestSuite {
    'SetState {
      val newState = List(ClientExercise("foo", Seq("", "")))
      val action = SetState(newState)
      val state = List()
      assert(Program.updateState(state, action) == newState)
    }
    'UpdateExercise {
      val action = UpdateExercise("foo", Seq("one", "two"))
      val state = List(ClientExercise("foo", Seq("", "")))
      val newState = Program.updateState(state, action)
      assert(newState(0).arguments == Seq("one", "two"))
    }
  }
}
