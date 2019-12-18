/*
 *  scala-exercises
 *
 *  Copyright 2015-2019 47 Degrees, LLC. <http://www.47deg.com>
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

package org.scalaexercises.runtime

import scala.collection.immutable.List
import scala.reflect.ClassTag
import java.net.URLClassLoader
import java.io.File

import cats.implicits._
import org.clapper.classutil.ClassFinder
import org.objectweb.asm.Opcodes
import org.scalaexercises.evaluator.types.Dependency
import org.scalaexercises.runtime.model._

import scala.util.Try

object Exercises {
  val LIBRARIES_PACKAGE = "org.scalaexercises.content"

  private[this] def classMap(cl: ClassLoader) = {
    val files = cl
      .asInstanceOf[URLClassLoader]
      .getURLs
      .map(url => new File(url.getFile)) filter (_.exists)
    val classFinder = ClassFinder(files, Some(Opcodes.ASM7))
    val classes = classFinder.getClasses
      .filter(Try(_).isSuccess)
      .toList
    ClassFinder.classInfoMap(classes.iterator)
  }

  private[this] def subclassesOf[A: ClassTag](cl: ClassLoader): List[String] = {
    def loop(currentClassLoader: ClassLoader, acc: List[String]): List[String] =
      Option(currentClassLoader) match {
        case None ⇒ acc
        case Some(cll: URLClassLoader) ⇒
          val cn = ClassFinder
            .concreteSubclasses(implicitly[ClassTag[A]].runtimeClass.getName, classMap(cll))
            .filter(_.name.startsWith(LIBRARIES_PACKAGE))
            .map(_.name)
            .toList
          loop(currentClassLoader.getParent, acc ++ cn)
        case Some(o) ⇒ loop(o.getParent, acc)
      }
    loop(cl, Nil)
  }

  def discoverLibraries(
      cl: ClassLoader = classOf[Exercise].getClassLoader): (List[String], List[Library]) = {
    val classNames: List[String] = subclassesOf[Library](cl)

    val errorsAndLibraries = classNames.map { name ⇒
      for {
        loadedClass ← guard(Class.forName(name, true, cl), s"$name not found")
        loadedObject ← guard(
          loadedClass.getField("MODULE$").get(null),
          s"$name must be defined as an object")
        loadedLibrary ← guard(loadedObject.asInstanceOf[Library], s"$name must extend Library")
      } yield loadedLibrary
    }

    errorsAndLibraries.separate
  }

  private def guard[A](f: ⇒ A, message: ⇒ String) =
    Either.catchNonFatal(f).leftMap(_ ⇒ message)

  def buildEvaluatorRequest(
      pkg: String,
      qualifiedMethod: String,
      rawArgs: List[String],
      imports: List[String] = Nil,
      resolvers: List[String],
      libraryDependencies: List[String]
  ): (List[String], List[Dependency], String) = {

    val extractEvaluatorResolvers: List[String] = {
      resolvers.filter(r => !r.isEmpty && r.contains("http")) map { resolver ⇒
        resolver.substring(resolver.indexOf("http"))
      }
    }

    val extractEvaluatorDependencies: List[Dependency] = {
      libraryDependencies map { dep ⇒
        val depArray = dep.split(":")
        Dependency(groupId = depArray(0), artifactId = depArray(1), version = depArray(2))
      }
    }

    val pre  = (s"import $pkg._" :: imports).mkString("; ")
    val code = s"""$qualifiedMethod(${rawArgs.mkString(", ")})"""

    val allCode = s"{$pre; $code}"

    (extractEvaluatorResolvers, extractEvaluatorDependencies, allCode)
  }
}
