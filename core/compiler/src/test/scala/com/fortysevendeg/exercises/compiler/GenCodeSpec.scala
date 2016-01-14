package com.fortysevendeg.exercises
package compiler

import org.scalatest._

class GenCodeSpec extends FunSpec with Matchers {

  describe("it") {
    it("should work") {

      import scala.tools.reflect.ToolBox
      import scala.reflect.runtime.{ currentMirror ⇒ m }

      val toolbox = m.mkToolBox()

      println(toolbox)

      val cg = CodeGen[toolbox.u.type](toolbox.u)
      import toolbox.u._

      val exercises = List(
        cg.emitExercise(Some("Example1")),
        cg.emitExercise(Some("Example2"))
      )

      val sections = List(
        cg.emitSection(
          name = "Section 1",
          exerciseTerms = exercises.map(_._1)
        ),
        cg.emitSection(
          name = "Section 2"
        )
      )

      val library = cg.emitLibrary(
        name = "MyLibrary",
        description = "This is my library",
        color = "#FFFFFF",
        sectionTerms = sections.map(_._1)
      )

      val tree = cg.emitPackage(
        packageName = "fail.sauce",
        libraryTree = library._2,
        sectionTrees = sections.map(_._2),
        exerciseTrees = exercises.map(_._2)
      )

      def transmogrify(tree: Tree): Tree = (tree match {
        case q"package $name { ..$stats }" ⇒
          stats
            .collectFirst { case ModuleDef(_, libraryTerm, _) ⇒ q"..$stats;$libraryTerm" }
        case _ ⇒ None
      }).getOrElse(EmptyTree)

      val runtimeLibrary = toolbox.eval(transmogrify(tree)).asInstanceOf[Library]

      import cg.EZTree._
      println(tree.code)
      println("~" * 20)
      println(runtimeLibrary.name)
      println(runtimeLibrary.description)

    }

  }

}
