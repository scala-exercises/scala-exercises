package com.fortysevendeg.exercises

import org.scalatest._

class LibraryDiscoverySpec extends FunSpec with Matchers {

  import LibraryDiscoverySpec._

  describe("exercise discovery") {
    it("should be able to load libraries from one to many resources") {

      {
        info("two libraries, described in separate resources")
        val (errors, discovered) = Exercises.discoverLibraries(RawDataResourceClassLoader(
          Exercises.LIBRARY_FILE_PATH → List(
            LibraryA.getClass.getName,
            LibraryB.getClass.getName
          )
        ))

        errors should equal(Nil)

        discovered should equal(
          LibraryA :: LibraryB :: Nil
        )
      }

      {
        info("three libraries, described in separate resources")
        val (errors, discovered) = Exercises.discoverLibraries(RawDataResourceClassLoader(
          Exercises.LIBRARY_FILE_PATH → List(
            LibraryA.getClass.getName,
            LibraryB.getClass.getName,
            LibraryC.getClass.getName
          )
        ))

        errors should equal(Nil)

        discovered should equal(
          LibraryA :: LibraryB :: LibraryC :: Nil
        )
      }

      {
        info("three libraries, two of them described in the same resource")
        val (errors, discovered) = Exercises.discoverLibraries(RawDataResourceClassLoader(
          Exercises.LIBRARY_FILE_PATH → List(
            s"""
          ${LibraryA.getClass.getName}
          ${LibraryB.getClass.getName}""",
            LibraryC.getClass.getName
          )
        ))

        errors should equal(Nil)

        discovered should equal(
          LibraryA :: LibraryB :: LibraryC :: Nil
        )
      }

      {
        info("three libraries, all of them described in the same resource")
        val (errors, discovered) = Exercises.discoverLibraries(RawDataResourceClassLoader(
          Exercises.LIBRARY_FILE_PATH → List(
            s"""
          ${LibraryA.getClass.getName}
          ${LibraryB.getClass.getName}
          ${LibraryC.getClass.getName}"""
          )
        ))

        errors should equal(Nil)

        discovered should equal(
          LibraryA :: LibraryB :: LibraryC :: Nil
        )
      }
    }

    it("should give errors when unable load a library") {

      {
        info("one bad library")
        val (errors, discovered) = Exercises.discoverLibraries(RawDataResourceClassLoader(
          Exercises.LIBRARY_FILE_PATH → List(
            classOf[BadLibraryA].getName
          )
        ))

        errors should have length 1

        discovered should equal(Nil)
      }

    }

  }
}

object LibraryDiscoverySpec {

  sealed trait DummyLibrary extends Library {
    override def color = ???
    override def description = ???
    override def name = ???
    override def sections = ???
  }

  object LibraryA extends DummyLibrary {

  }

  object LibraryB extends DummyLibrary {

  }

  object LibraryC extends DummyLibrary {

  }

  class BadLibraryA extends DummyLibrary {

  }
}
