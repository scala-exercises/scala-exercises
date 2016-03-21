package com.fortysevendeg.exercises
package compiler

import scala.tools.reflect.ToolBox

import org.scalatest._

class TreeGenSpec extends FunSpec with Matchers {

  val toolbox = scala.reflect.runtime.currentMirror.mkToolBox()
  import toolbox.u._

  describe("code tree generation") {

    val treeGen = new TreeGen[toolbox.u.type](toolbox.u)

    it("should generate a tree that can be eval'd") {

      val exercises = List(
        treeGen.makeExercise(
          name = "Example1",
          description = None,
          code = "code",
          qualifiedMethod = "foo.bar",
          imports = Nil,
          explanation = None
        ),
        treeGen.makeExercise(
          name = "Example2",
          description = None,
          code = "code",
          qualifiedMethod = "foo.bar",
          imports = Nil,
          explanation = None
        )
      )

      val sections = List(
        treeGen.makeSection(
          name = "Section 1",
          description = Some("This is section 1"),
          exerciseTerms = exercises.map(_._1),
          imports = Nil
        ),
        treeGen.makeSection(
          name = "Section 2",
          description = Some("This is section 2"),
          exerciseTerms = Nil,
          imports = Nil
        )
      )

      val library = treeGen.makeLibrary(
        name = "MyLibrary",
        description = "This is my library",
        color = Some("#FFFFFF"),
        sectionTerms = sections.map(_._1)
      )

      val tree = treeGen.makePackage(
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
