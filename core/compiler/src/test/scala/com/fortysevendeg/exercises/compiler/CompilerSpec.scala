package com.fortysevendeg.exercises
package compiler

import scala.language.existentials

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
      /** Sample Library
        * This is a sample library.
        */
      object SampleLibrary extends exercise.Library {
        override def sections = List(
          Section1
        )
      }

      /** Section 1
        * This is section 1.
        * It has a multi line description.
        */
      object Section1 extends exercise.Section {
        /** This is example exercise 1! */
        def example1() { }

        /** This is example exercise 2! */
        def example2() { }
      }
      """

      val classLoader = globalUtil.load(code)
      val library = classLoader
        .loadClass("SampleLibrary$")
        .getField("MODULE$").get(null)
        .asInstanceOf[exercise.Library]

      val res = Compiler.compile(library, code :: Nil, "sample")

      assert(res.isRight)

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
