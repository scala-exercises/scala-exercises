/*
 * scala-exercises-runtime
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises

import scala.annotation.tailrec
import scala.collection.JavaConversions._

import java.net.URL
import java.nio.charset.Charset
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.ClassNotFoundException
import java.nio.charset.StandardCharsets

import cats.data.Xor

object Exercises {

  private[exercises] val LIBRARY_FILE_PATH = "scala-exercises/library.47"

  def discoverLibraries(cl: ClassLoader = classOf[Exercise].getClassLoader) = {

    /** Reads the lines, in reverse order, from a URL resource. */
    def readLinesReverse(url: URL): List[String] = {
      val in = new BufferedReader(new InputStreamReader(url.openStream, StandardCharsets.UTF_8))
      @tailrec def accLines(lines: List[String] = Nil): List[String] = {
        val line = in.readLine()
        if (line eq null) lines else accLines(line :: lines)
      }
      accLines()
    }

    val classNames = cl.getResources(LIBRARY_FILE_PATH)
      .foldLeft(Nil: List[String]) { (acc, url) ⇒ readLinesReverse(url) ++ acc }
      .reverse
      .map(_.trim)
      .filterNot(_.isEmpty)

    val (errors, libraries) = classNames.foldLeft((Nil: List[String], Nil: List[Library])) { (acc, name) ⇒

      val loadedLibrary = for {
        loadedClass ← guard(Class.forName(name, true, cl), s"${name} not found")
        loadedObject ← guard(loadedClass.getField("MODULE$").get(null), s"${name} must be defined as an object")
        loadedLibrary ← guard(loadedObject.asInstanceOf[Library], s"${name} must extend Library")
      } yield loadedLibrary

      // until a bifoldable exists in Cats...
      loadedLibrary match {
        case Xor.Right(c) ⇒ (acc._1, c :: acc._2)
        case Xor.Left(e)  ⇒ (e :: acc._1, acc._2)
      }
    }

    (errors, libraries.reverse)
  }

  private def guard[A](f: ⇒ A, message: ⇒ String) =
    Xor.catchNonFatal(f).leftMap(_ ⇒ message)

}
