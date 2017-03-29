/*
 *  scala-exercises
 *
 *  Copyright 2015-2017 47 Degrees, LLC. <http://www.47deg.com>
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

package org.scalaexercises.compiler

import scala.tools.reflect.ToolBox

import org.scalaexercises.runtime.model.Library

import org.scalatest._

class TreeGenSpec extends FunSpec with Matchers {

  val toolbox = scala.reflect.runtime.currentMirror.mkToolBox()
  import toolbox.u._

  describe("code tree generation") {

    val treeGen = new TreeGen[toolbox.u.type](toolbox.u)

    it("should generate a tree that can be eval'd") {

      val exercises = List(
        treeGen.makeExercise(
          libraryName = "MyLibrary",
          name = "Example1",
          description = None,
          code = "code",
          packageName = "foo",
          qualifiedMethod = "foo.bar",
          imports = Nil,
          explanation = None
        ),
        treeGen.makeExercise(
          libraryName = "MyLibrary",
          name = "Example2",
          description = None,
          code = "code",
          packageName = "foo",
          qualifiedMethod = "foo.bar",
          imports = Nil,
          explanation = None
        )
      )

      val sections = List(
        treeGen.makeSection(
          libraryName = "MyLibrary",
          name = "Section 1",
          description = Some("This is section 1"),
          exerciseTerms = exercises.map(_._1),
          imports = Nil,
          contributionTerms = Nil
        ),
        treeGen.makeSection(
          libraryName = "MyLibrary",
          name = "Section 2",
          description = Some("This is section 2"),
          exerciseTerms = Nil,
          imports = Nil,
          contributionTerms = Nil
        )
      )

      val buildInfo = treeGen.makeBuildInfo(
        name = "MyLibrary",
        resolvers = Nil,
        libraryDependencies = Nil
      )

      val library = treeGen.makeLibrary(
        name = "MyLibrary",
        description = "This is my library",
        color = Some("#FFFFFF"),
        logoPath = "logoPath",
        logoData = None,
        sectionTerms = sections.map(_._1),
        owner = "scala-exercises",
        repository = "site",
        timestamp = "1-1-1970",
        buildInfoT = buildInfo._1
      )

      val tree = treeGen.makePackage(
        packageName = "fail.sauce",
        trees = (library._2 :: sections.map(_._2) ::: exercises.map(_._2)) :+ buildInfo._2
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
