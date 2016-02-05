package com.fortysevendeg.exercises
package compiler

import scala.tools.reflect.ToolBox

import org.scalatest._

class GenCodeSpec extends FunSpec with Matchers {

  val toolbox = scala.reflect.runtime.currentMirror.mkToolBox()
  import toolbox.u._

  describe("code tree emission") {

    val emitter = new TreeEmitters[toolbox.u.type] {
      override val u = toolbox.u
    }

    it("should generate a tree that can be eval'd") {

      val exercises = List(
        emitter.emitExercise(
          name = Some("Example1"),
          description = None,
          code = None,
          explanation = None
        ),
        emitter.emitExercise(
          name = Some("Example2"),
          description = None,
          code = None,
          explanation = None
        )
      )

      val sections = List(
        emitter.emitSection(
          name = "Section 1",
          description = Some("This is section 1"),
          exerciseTerms = exercises.map(_._1)
        ),
        emitter.emitSection(
          name = "Section 2",
          description = Some("This is section 2"),
          exerciseTerms = Nil
        )
      )

      val library = emitter.emitLibrary(
        name = "MyLibrary",
        description = "This is my library",
        color = "#FFFFFF",
        sectionTerms = sections.map(_._1)
      )

      val tree = emitter.emitPackage(
        packageName = "fail.sauce",
        trees = library._2 :: sections.map(_._2) ::: exercises.map(_._2)
      )

      val runtimeLibrary = evalLibrary(tree)
      runtimeLibrary.name should equal("MyLibrary")
    }

  }

  def evalLibrary(tree: Tree) = {
    val evalableTree = (tree match {
      case q"package $name { ..$stats }" ⇒
        stats
          .collectFirst { case ModuleDef(_, libraryTerm, _) ⇒ q"..$stats;$libraryTerm" }
      case _ ⇒ None
    }).getOrElse(EmptyTree)

    toolbox.eval(evalableTree).asInstanceOf[Library]
  }

}
