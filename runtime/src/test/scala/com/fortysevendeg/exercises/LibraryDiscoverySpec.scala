/*
 * scala-exercises-runtime
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises

import org.scalatest._

class LibraryDiscoverySpec extends FunSpec with Matchers {

  import defaultLib._

  val cl = classOf[Exercise].getClassLoader

  describe("exercise discovery") {
    it("should be able to load libraries") {
      val (errors, discovered) = Exercises.discoverLibraries()

      discovered.toSet shouldEqual Set(
        LibraryA, LibraryB, LibraryC
      )
    }

    it("libraries that are not objects should trigger errors") {
      val (errors, discovered) = Exercises.discoverLibraries()

      errors.size shouldEqual 1
    }
  }
}
