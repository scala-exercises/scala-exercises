/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises.services

import cats.data.Xor
import java.io.File
import java.net.URLClassLoader

import services.exercisev0.BooleanTypeConversion
import com.toddfast.util.convert.TypeConverter
import shared._
import org.clapper.classutil.{ ClassInfo, ClassFinder }
import play.api.Play

import scala.reflect.ClassTag
import scala.reflect.runtime.{ universe ⇒ ru }
import ru._
import cats.syntax.xor._

/** Main entry point and service for libraries, categories and exercises discovery + evaluation
  */
object ExercisesService {
  TypeConverter.registerTypeConversion(new BooleanTypeConversion())

  private[this] lazy val classMap = {
    val cl = Play.maybeApplication map (_.classloader) getOrElse ClassLoader.getSystemClassLoader
    val files = cl.asInstanceOf[URLClassLoader].getURLs map (_.getFile)
    val classFinder = ClassFinder(files map (new File(_)) filter (f ⇒ f.exists()))
    val classes = classFinder.getClasses.toIterator
    ClassFinder.classInfoMap(classes)
  }

  private[this] def subclassesOf[A: ClassTag] =
    ClassFinder.concreteSubclasses(implicitly[ClassTag[A]].runtimeClass.getName, classMap).toList

  private[this] def sources(classInfo: ClassInfo) = {
    val exerciseClass = Class.forName(classInfo.name)
    val sourcesStream = exerciseClass.getResourceAsStream(exerciseClass.getSimpleName + ".scala")
    val sources = scala.io.Source.fromInputStream(sourcesStream).getLines().toList
    sourcesStream.close()
    sources
  }

  private[this] def simpleClassName(classInfo: ClassInfo) =
    Class.forName(classInfo.name).asInstanceOf[Class[_ <: exercise.Section]].getSimpleName

  private[this] def packageObjectSource(classInfo: ClassInfo) = {
    val exerciseClass = Class.forName(classInfo.name)
    val sourcesStream = exerciseClass.getResourceAsStream("package.scala")
    val sources = scala.io.Source.fromInputStream(sourcesStream).getLines().toList
    sourcesStream.close()
    sources
  }

  private[this] def evaluate(evaluation: ExerciseEvaluation, classInfo: ClassInfo): Throwable Xor Unit = {
    val targetSectionInstance = Class.forName(classInfo.name).newInstance()
    val mirror = runtimeMirror(getClass.getClassLoader)
    val targetMirror = mirror.reflect(targetSectionInstance)
    val method = targetMirror.symbol.typeSignature.decl(TermName(evaluation.method)).asMethod
    val methodMirror = targetMirror.reflectMethod(method)
    val argsWithTypes = evaluation.args zip method.paramLists.flatten
    val argValues = argsWithTypes map {
      case (arg, symbol) ⇒
        val argClass = mirror.runtimeClass(symbol.typeSignature.dealias)
        TypeConverter.convert(argClass, arg)
    }
    methodMirror.apply(argValues: _*).asInstanceOf[Throwable Xor Unit]
  }

  val (libraries, librarySections) = {
    val (errors, libraries0) = Exercises.discoverLibraries(cl = ExercisesService.getClass.getClassLoader)
    val libraries1 = colorize(libraries0)
    errors.foreach(error ⇒ Logger.warn(s"$error")) // TODO: handle errors better?
    (
      libraries1.map(convertLibrary),
      libraries1.map(library0 ⇒ library0.name → library0.sections.map(convertSection)).toMap
    )
  }

  /** Scans the classpath returning a list of sections containing exercises for a given library
    */
  def section(libraryName: String, sectionName: String): Option[Section] = for {
    pkg ← subclassesOf[exercise.Library].headOption
    library ← ExerciseCodeExtractor.buildLibrary(packageObjectSource(pkg))
    if library.name == libraryName
    subclass ← subclassesOf[exercise.Section].headOption
    if simpleClassName(subclass) == sectionName
    section ← ExerciseCodeExtractor.buildSection(sources(subclass))
  } yield section

  /** Evaluates an exercise in a given section and category and returns a disjunction
    * containing either an exception or Unit representing success
    */
  def evaluate(evaluation: ExerciseEvaluation): Throwable Xor Unit = {
    val evalResult = for {
      pkg ← subclassesOf[exercise.Library]
      library ← ExerciseCodeExtractor.buildLibrary(packageObjectSource(pkg)).toList
      if library.name == evaluation.libraryName
      subclass ← subclassesOf[exercise.Section]
      if simpleClassName(subclass) == evaluation.sectionName
    } yield evaluate(evaluation, subclass)
    evalResult.headOption match {
      case None         ⇒ new RuntimeException("Evaluation produced no results").left[Unit]
      case Some(result) ⇒ result
    }
    Xor.right(Unit)
  }
}

sealed trait RuntimeSharedConversions {
  import com.fortysevendeg.exercises._

  // not particularly clean, but this assigns colors
  // to libraries that don't have a default color provided
  // TODO: make this nicer
  def colorize(libraries: List[Library]): List[Library] = {
    libraries
    val autoPalette = List(
      "#00587A",
      "#44BBFF",
      "#EBF680",
      "#66CC99",
      "#FCA65F",
      "#112233",
      "#FC575E",
      "#CDCBA6",
      "#37465D",
      "#DD6F47",
      "#6AB0AA",
      "#008891",
      "#0F3057"
    )

    val (_, res) = libraries.foldLeft((autoPalette, Nil: List[Library])) { (acc, library) ⇒
      val (colors, librariesAcc) = acc
      if (library.color.isEmpty) {
        val (color, colors0) = colors match {
          case head :: tail ⇒ Some(head) → tail
          case Nil          ⇒ None → Nil
        }
        colors0 → (DefaultLibrary(
          name = library.name,
          description = library.description,
          color = color,
          sections = library.sections
        ) :: librariesAcc)
      } else
        colors → (library :: librariesAcc)
    }
    res.reverse
  }

  def convertLibrary(library: Library) =
    shared.Library(
      name = library.name,
      description = library.description,
      color = library.color getOrElse "black",
      sectionNames = library.sections.map(_.name)
    )

  def convertSection(section: Section) =
    shared.Section(
      name = section.name,
      description = section.description,
      exercises = section.exercises.map(convertExercise)
    )

  def convertExercise(exercise: Exercise) =
    shared.Exercise(
      method = None, // exercise.eval Option[type Input => Unit]
      name = exercise.name,
      description = exercise.code,
      code = exercise.code,
      explanation = exercise.explanation
    )

}
