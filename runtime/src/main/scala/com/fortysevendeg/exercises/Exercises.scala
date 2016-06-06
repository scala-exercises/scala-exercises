/*
 * scala-exercises-runtime
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises

import scala.annotation.tailrec
import scala.collection.immutable.List
import scala.collection.JavaConversions._
import scala.reflect.ClassTag

import java.net.URLClassLoader
import java.io.File
import java.lang.ClassNotFoundException

import cats.data.Xor

import org.clapper.classutil.ClassFinder

object Exercises {
  val LIBRARIES_PACKAGE = "defaultLib"

  private[this] def classMap(cl: ClassLoader) = {
    val files = cl.asInstanceOf[URLClassLoader].getURLs map (_.getFile)
    val classFinder = ClassFinder(files map (new File(_)) filter (f ⇒ f.exists()))
    val classes = classFinder.getClasses.toIterator
    ClassFinder.classInfoMap(classes)
  }

  private[this] def subclassesOf[A: ClassTag](cl: ClassLoader) =
    ClassFinder.concreteSubclasses(implicitly[ClassTag[A]].runtimeClass.getName, classMap(cl))
      .filter(_.name.startsWith(LIBRARIES_PACKAGE))
      .map(_.name)
      .toList

  def discoverLibraries(cl: ClassLoader = classOf[Exercise].getClassLoader) = {
    val classNames: List[String] = subclassesOf[Library](cl)

    val (errors, libraries) = classNames.foldLeft((Nil: List[String], Nil: List[Library])) { (acc, name) ⇒
      val loadedLibrary = for {
        loadedClass ← guard(Class.forName(name, true, cl), s"${name} not found")
        loadedObject ← guard(loadedClass.getField("MODULE$").get(null), s"${name} must be defined as an object")
        loadedLibrary ← guard(loadedObject.asInstanceOf[Library], s"${name} must extend Library")
      } yield loadedLibrary

      // until a bifoldable exists in Cats...
      loadedLibrary match {
        case Xor.Right(c) ⇒ (acc._1, c :: acc._2)
        case Xor.Left(e) ⇒ (e :: acc._1, acc._2)
      }
    }

    (errors, libraries)
  }

  private def guard[A](f: ⇒ A, message: ⇒ String) =
    Xor.catchNonFatal(f).leftMap(_ ⇒ message)

}
