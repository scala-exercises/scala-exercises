package com.fortysevendeg.exercises
package compiler

import scala.tools.reflect.ToolBox

import org.scalatest._

class GenCodeSpec extends FunSpec with Matchers {

  val toolbox = scala.reflect.runtime.currentMirror.mkToolBox()
  val cg = CodeGen[toolbox.u.type](toolbox.u)

  import toolbox.u._

  def evalLibrary(tree: Tree) = {
    val evalableTree = (tree match {
      case q"package $name { ..$stats }" ⇒
        stats
          .collectFirst { case ModuleDef(_, libraryTerm, _) ⇒ q"..$stats;$libraryTerm" }
      case _ ⇒ None
    }).getOrElse(EmptyTree)

    toolbox.eval(evalableTree).asInstanceOf[Library]
  }

  describe("code generation") {
    it("should generate a tree that can be eval'd") {

      val exercises = List(
        cg.emitExercise(
          name = Some("Example1"),
          description = None,
          code = None,
          explanation = None
        ),
        cg.emitExercise(
          name = Some("Example2"),
          description = None,
          code = None,
          explanation = None
        )
      )

      val sections = List(
        cg.emitSection(
          name = "Section 1",
          description = Some("This is section 1"),
          exerciseTerms = exercises.map(_._1)
        ),
        cg.emitSection(
          name = "Section 2",
          description = Some("This is section 2"),
          exerciseTerms = Nil
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

      val runtimeLibrary = evalLibrary(tree)
      runtimeLibrary.name should equal("MyLibrary")
    }

  }

}
