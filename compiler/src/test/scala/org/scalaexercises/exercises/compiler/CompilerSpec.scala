package org.scalaexercises.compiler

import org.scalaexercises.definitions.Library
import org.scalatest._

import scala.reflect.internal.util.{ AbstractFileClassLoader, BatchSourceFile }
import scala.tools.nsc.io.VirtualDirectory
import scala.tools.nsc.{ Global, Settings }

class CompilerSpec extends FunSpec with Matchers {

  describe("library compilation") {
    it("works") {

      val code = """
      /** This is the sample library.
        * @param name Sample Library
        */
      object SampleLibrary extends org.scalaexercises.definitions.Library {
        override def owner = "scala-exercises"
        override def repository = "site"
        override def sections = List(
          Section1
        )
      }

      /** This is section 1.
        * It has a multi line description.
        *
        * @param name Section 1
        */
      object Section1 extends org.scalaexercises.definitions.Section {
        /** This is example exercise 1! */
        def example1() = { 1 }

        /** This is example exercise 2! */
        def example2() = {
          println("this is some code!")
          println("does it work?")
          (5 + 500)
        }
      }
      """

      val classLoader = globalUtil.load(code)
      val library = classLoader
        .loadClass("SampleLibrary$")
        .getField("MODULE$").get(null)
        .asInstanceOf[Library]

      val path = "(internal)"
      val res = Compiler().compile(library, code :: Nil, path :: Nil, "/", "sample", fetchContributors = false)
      assert(res.isRight, s"""; ${res.fold(identity, _ ⇒ "")}""")
    }

    it("fails if sections list is empty") {

      val code = """
      /** This is the sample library.
        * @param name Sample Library
        */
      object SampleLibrary extends org.scalaexercises.definitions.Library {
        override def owner = "scala-exercises"
        override def repository = "site"
        override def sections = Nil
      }"""

      val classLoader = globalUtil.load(code)
      val library = classLoader
        .loadClass("SampleLibrary$")
        .getField("MODULE$").get(null)
        .asInstanceOf[Library]

      val path = "(internal)"
      val res = Compiler().compile(library, code :: Nil, path :: Nil, "/", "sample", fetchContributors = false)
      assert(res.isLeft, s"""; ${res.fold(identity, _ ⇒ "")}""")
    }
  }

  object globalUtil {
    val global = new Global(new Settings {
      embeddedDefaults[CompilerSpec]
    })
    val outputTarget = new VirtualDirectory("(memory)", None)
    global.settings.outputDirs.setSingleOutput(outputTarget)

    def load(code: String): ClassLoader = {
      lazy val run = new global.Run
      run.compileSources(List(new BatchSourceFile("(inline)", code)))
      new AbstractFileClassLoader(outputTarget, classOf[CompilerSpec].getClassLoader)
    }
  }

}
