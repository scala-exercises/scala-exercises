/*
 *  scala-exercises
 *
 *  Copyright 2015-2019 47 Degrees, LLC. <http://www.47deg.com>
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

package org.scalaexercises.runtime

import org.scalaexercises.runtime.model.Exercise

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class LibraryDiscoverySpec extends AnyFunSpec with Matchers {

  import org.scalaexercises.content._

  val cl = classOf[Exercise].getClassLoader

  describe("exercise discovery") {
    it("should be able to load libraries") {
      val (errors, discovered) = Exercises.discoverLibraries()

      discovered.toSet shouldEqual Set(
        LibraryA,
        LibraryB,
        LibraryC
      )
    }

    it("libraries that are not objects should trigger errors") {
      val (errors, discovered) = Exercises.discoverLibraries()

      errors.size shouldEqual 1
    }
  }
}
