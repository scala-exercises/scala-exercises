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

package org.scalaexercises.client
package programspec

import utest._

import actions._
import state.{State}
import factories.Factories._

object StateSpec extends TestSuite {
  def tests = TestSuite {
    'SetState {
      val newState = List(clientExercise(method = "foo", args = Seq("", "")))
      val action   = SetState(newState)
      val state    = List()
      assert(State.update(state, action) == newState)
    }
    'UpdateExercise {
      val action   = UpdateExercise("foo", Seq("one", "two"))
      val state    = List(clientExercise(method = "foo", args = Seq("", "")))
      val newState = State.update(state, action)
      assert(newState(0).arguments == Seq("one", "two"))
    }
  }
}
