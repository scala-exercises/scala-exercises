/*
 * scala-exercises-runtime
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.runtime

import org.scalaexercises.runtime.model._

import scala.collection.immutable.List
import scala.reflect.ClassTag
import java.net.URLClassLoader
import java.io.File

import cats.data.Xor
import org.clapper.classutil.ClassFinder
import org.scalaexercises.evaluator.Dependency

object Exercises {
  val LIBRARIES_PACKAGE = "org.scalaexercises.content"

  private[this] def classMap(cl: ClassLoader) = {
    val files = cl.asInstanceOf[URLClassLoader].getURLs map (_.getFile)
    val classFinder = ClassFinder(files map (new File(_)) filter (f ⇒ f.exists()))
    val classes = classFinder.getClasses.toIterator
    ClassFinder.classInfoMap(classes)
  }

  private[this] def subclassesOf[A: ClassTag](cl: ClassLoader): List[String] = {
    def loop(currentClassLoader: ClassLoader, acc: List[String]): List[String] = Option(currentClassLoader) match {
      case None ⇒ acc
      case Some(cll: URLClassLoader) ⇒
        val cn = ClassFinder.concreteSubclasses(implicitly[ClassTag[A]].runtimeClass.getName, classMap(cll))
          .filter(_.name.startsWith(LIBRARIES_PACKAGE))
          .map(_.name)
          .toList
        loop(currentClassLoader.getParent, acc ++ cn)
      case Some(o) ⇒ loop(o.getParent, acc)
    }
    loop(cl, Nil)
  }

  def discoverLibraries(cl: ClassLoader = classOf[Exercise].getClassLoader) = {
    val classNames: List[String] = subclassesOf[Library](cl)

    val (errors, libraries) = classNames.foldLeft((Nil: List[String], Nil: List[Library])) { (acc, name) ⇒
      val loadedLibrary = for {
        loadedClass ← guard(Class.forName(name, true, cl), s"$name not found")
        loadedObject ← guard(loadedClass.getField("MODULE$").get(null), s"$name must be defined as an object")
        loadedLibrary ← guard(loadedObject.asInstanceOf[Library], s"$name must extend Library")
      } yield loadedLibrary

      // until a bifoldable exists in Cats...
      loadedLibrary match {
        case Xor.Right(c) ⇒ (acc._1, c :: acc._2)
        case Xor.Left(e)  ⇒ (e :: acc._1, acc._2)
      }
    }

    (errors, libraries)
  }

  private def guard[A](f: ⇒ A, message: ⇒ String) =
    Xor.catchNonFatal(f).leftMap(_ ⇒ message)

  def buildEvaluatorRequest(
    pkg:                 String,
    qualifiedMethod:     String,
    rawArgs:             List[String],
    imports:             List[String] = Nil,
    resolvers:           List[String],
    libraryDependencies: List[String]
  ): (List[String], List[Dependency], String) = {

    val extractEvaluatorResolvers: List[String] = {
      resolvers.filter(!_.isEmpty) map { resolver ⇒
        resolver.substring(resolver.indexOf("http"))
      }
    }

    val extractEvaluatorDependencies: List[Dependency] = {
      libraryDependencies map { dep ⇒
        val depArray = dep.split(":")
        Dependency(groupId = depArray(0), artifactId = depArray(1), version = depArray(2))
      }
    }

    val pre = (s"import $pkg._" :: imports).mkString("; ")
    val code = s"""$qualifiedMethod(${rawArgs.mkString(", ")})"""

    val allCode = s"{$pre; $code}"

    (extractEvaluatorResolvers, extractEvaluatorDependencies, allCode)
  }
}
