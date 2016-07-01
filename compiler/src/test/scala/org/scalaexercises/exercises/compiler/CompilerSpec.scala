package org.scalaexercises.exercises
package compiler

import scala.reflect.internal.util.AbstractFileClassLoader
import scala.reflect.internal.util.BatchSourceFile
import scala.tools.nsc.{ Global, Settings }
import scala.tools.nsc.io.{ VirtualDirectory, AbstractFile }

import java.lang.ClassLoader

import org.scalatest._

class CompilerSpec extends FunSpec with Matchers {

  describe("library compilation") {
    it("works") {

      val code = """
      /** This is the sample library.
        * @param name Sample Library
        */
      object SampleLibrary extends exercise.Library {
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
      object Section1 extends exercise.Section {
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
        .asInstanceOf[exercise.Library]

      val path = "(internal)"
      val res = Compiler().compile(library, code :: Nil, path :: Nil, "/", "sample")
      assert(res.isRight, s"""; ${res.fold(identity, _ ⇒ "")}""")
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
