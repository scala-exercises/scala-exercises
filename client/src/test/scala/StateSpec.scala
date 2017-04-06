/*
 * scala-exercises-client
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.client
package programspec

import utest._

import actions._
import state.{ State }
import factories.Factories._

object StateSpec extends TestSuite {
  def tests = TestSuite {
    'SetState{
      val newState = List(clientExercise(method = "foo", args = Seq("", "")))
      val action = SetState(newState)
      val state = List()
      assert(State.update(state, action) == newState)
    }
    'UpdateExercise{
      val action = UpdateExercise("foo", Seq("one", "two"))
      val state = List(clientExercise(method = "foo", args = Seq("", "")))
      val newState = State.update(state, action)
      assert(newState(0).arguments == Seq("one", "two"))
    }
  }
}
