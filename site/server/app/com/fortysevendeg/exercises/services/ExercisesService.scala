package com.fortysevendeg.exercises.services

import cats.data.Xor
import java.io.File
import java.net.URLClassLoader

import services.exercisev0.BooleanTypeConversion
import com.toddfast.util.convert.TypeConverter
import shared._
import org.clapper.classutil.{ ClassInfo, ClassFinder }
import play.api.Play

import scala.language.implicitConversions
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

  /** Scans the classpath returning a list of all libraries found
    * A section is defined by a folder with a package object including section information.
    * Library packages should be nested under `exercises`
    * @see exercises.stdlib
    */
  def libraries: List[Library] = for {
    subclass ← subclassesOf[exercise.Library]
    library ← ExerciseCodeExtractor.buildLibrary(packageObjectSource(subclass)).toList
    sections = subclassesOf[exercise.Section] filter (e ⇒ sources(e) contains s"package exercises.${library.name}")
    sectionNames = sections map (s ⇒ Class.forName(s.name).getSimpleName)
  } yield {
    library.copy(sectionNames = sectionNames)
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
  }

}
