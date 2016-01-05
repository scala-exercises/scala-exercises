package services.parser

import java.io.File
import java.net.URLClassLoader

import com.toddfast.util.convert.TypeConverter
import org.clapper.classutil.{ ClassInfo, ClassFinder }
import play.api.Play
import shared.ExerciseRunner.ExerciseResult
import shared.{ ExerciseEvaluation, SectionPkg, Exercises, Section, Category }

import scala.language.implicitConversions
import scala.reflect.ClassTag
import scala.reflect.runtime.{ universe ⇒ ru }
import ru._
import scalaz._, Scalaz._

/** Main entry point and service for sections, categories and exercises discovery + evaluation
  */
object ExercisesService {

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
    Class.forName(classInfo.name).asInstanceOf[Class[_ <: Exercises]].getSimpleName

  private[this] def packageObjectSource(classInfo: ClassInfo) = {
    val exerciseClass = Class.forName(classInfo.name)
    val sourcesStream = exerciseClass.getResourceAsStream("package.scala")
    val sources = scala.io.Source.fromInputStream(sourcesStream).getLines().toList
    sourcesStream.close()
    sources
  }

  private[this] def evaluate(evaluation: ExerciseEvaluation, classInfo: ClassInfo): ExerciseResult[Unit] = {
    val targetCategoryInstance = Class.forName(classInfo.name).newInstance()
    val mirror = runtimeMirror(getClass.getClassLoader)
    val targetMirror = mirror.reflect(targetCategoryInstance)
    val method = targetMirror.symbol.typeSignature.decl(TermName(evaluation.method)).asMethod
    val methodMirror = targetMirror.reflectMethod(method)
    val argsWithTypes = evaluation.args zip method.paramLists.flatten
    val argValues = argsWithTypes map {
      case (arg, symbol) ⇒
        val argClass = mirror.runtimeClass(symbol.typeSignature.dealias)
        TypeConverter.convert(argClass, arg)
    }
    methodMirror.apply(argValues: _*).asInstanceOf[ExerciseResult[Unit]]
  }

  /** Scans the classpath returning a list of all Sections found
    * A section is defined by a folder with a package object including section information.
    * Section packages should be nested under `exercises`
    * @see exercises.stdlib
    */
  def sections: List[Section] = for {
    subclass ← subclassesOf[SectionPkg]
    section ← ExerciseCodeExtractor.buildSection(packageObjectSource(subclass)).toList
    categories = subclassesOf[Exercises] filter (e ⇒ sources(e) contains s"package exercises.${section.title}")
    exerciseClasses = categories map (c ⇒ Class.forName(c.name).getSimpleName)
  } yield section.copy(categories = exerciseClasses)

  /** Scans the classpath returning a list of categories containing exercises for a given section
    */
  def category(section: String, category: String): List[Category] = for {
    pkg ← subclassesOf[SectionPkg]
    sct ← ExerciseCodeExtractor.buildSection(packageObjectSource(pkg)).toList
    if sct.title == section
    subclass ← subclassesOf[Exercises]
    if simpleClassName(subclass) == category
    category ← ExerciseCodeExtractor.buildCategory(sources(subclass))
  } yield category

  /** Evaluates an exercise in a given section and category and returns a disjunction
    * containing either an exception or Unit representing success
    */
  def evaluate(evaluation: ExerciseEvaluation): ExerciseResult[Unit] = {
    val evalResult = for {
      pkg ← subclassesOf[SectionPkg]
      sct ← ExerciseCodeExtractor.buildSection(packageObjectSource(pkg)).toList
      if sct.title == evaluation.section
      subclass ← subclassesOf[Exercises]
      if simpleClassName(subclass) == evaluation.category
    } yield evaluate(evaluation, subclass)
    evalResult.headOption match {
      case None         ⇒ new RuntimeException("Evaluation produced no results").left[Unit]
      case Some(result) ⇒ result
    }
  }

}