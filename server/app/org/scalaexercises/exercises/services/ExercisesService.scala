/*
 * Copyright 2014-2020 47 Degrees Open Source <https://www.47deg.com>
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
 */

package org.scalaexercises.exercises
package services

import cats.data.State
import cats.syntax.all._
import org.apache.commons.io.IOUtils
import org.clapper.classutil.ClassFinder
import org.objectweb.asm.Opcodes
import org.scalaexercises.definitions.{Library => DefnLibrary}
import org.scalaexercises.runtime.Exercises
import org.scalaexercises.runtime.Timestamp
import org.scalaexercises.runtime.model.DefaultLibrary
import org.scalaexercises.runtime.model.{BuildInfo => RuntimeBuildInfo}
import org.scalaexercises.runtime.model.{Contribution => RuntimeContribution}
import org.scalaexercises.runtime.model.{Exercise => RuntimeExercise}
import org.scalaexercises.runtime.model.{Library => RuntimeLibrary}
import org.scalaexercises.runtime.model.{Section => RuntimeSection}
import org.scalaexercises.types.evaluator.CoreDependency
import org.scalaexercises.types.exercises._
import play.api.Logger

import java.io.File
import java.net.URLClassLoader
import java.time.Instant
import java.util.Base64
import javax.inject.Singleton
import scala.reflect.ClassTag
import scala.util.Try

@Singleton
class ExercisesService(cl: ClassLoader) extends RuntimeSharedConversions {

  private def guard[A](f: => A, message: => String) =
    Either.catchNonFatal(f).leftMap(_ => message)

  private def classMap(cl: ClassLoader) = {
    val files = (cl
      .asInstanceOf[URLClassLoader]
      .getURLs
      .map(url => new File(url.getFile)) filter (_.exists)).toSeq
    val classFinder = ClassFinder(files, Some(Opcodes.ASM7))
    val classes = classFinder
      .getClasses()
      .filter(Try(_).isSuccess)
      .toList
    ClassFinder.classInfoMap(classes.iterator)
  }

  private def subclassesOf[A: ClassTag](cl: ClassLoader): List[String] = {
    def loop(currentClassLoader: ClassLoader, acc: List[String], iter: Int): List[String] = {
      Option(currentClassLoader) match {
        case None => acc
        case Some(cll: URLClassLoader) =>
          val subclasses = ClassFinder.concreteSubclasses(implicitly[ClassTag[A]].runtimeClass, classMap(cll))
          val cn = ClassFinder
            .concreteSubclasses(implicitly[ClassTag[A]].runtimeClass.getName, classMap(cll))
            .map(_.name)
            .toList
          loop(currentClassLoader.getParent, acc ++ cn, iter + 1)
        case Some(o) => loop(o.getParent, acc, iter + 1)
      }
    }
    loop(cl, Nil, 0)
  }

  private def discoverLibraries(
      cl: ClassLoader = classOf[DefnLibrary].getClassLoader
  ): (List[String], List[DefnLibrary]) = {
    val classNames: List[String] = subclassesOf[DefnLibrary](cl)

    val errorsAndLibraries = classNames.map { name =>
      for {
        loadedClass <- guard(Class.forName(name, true, cl), s"$name not found")
        loadedObject <- guard(
          loadedClass.getField("MODULE$").get(null),
          s"$name must be defined as an object"
        )
        loadedLibrary <- guard(loadedObject.asInstanceOf[DefnLibrary], s"$name must extend Library")
      } yield loadedLibrary
    }

    errorsAndLibraries.separate
  }

  private lazy val logger = Logger(this.getClass)

  val discoveredViaOverride = discoverLibraries()

  lazy val (errors, runtimeLibraries) = (Nil, List(
    new RuntimeLibrary {
      def owner = "James"
      def repository = "https://github.com/jisantuc/cliffs"
      def name = "example"
      def description = "passing an actual value"
      def color = Some("#e7816f")
      def logoPath = "https://cdn.vox-cdn.com/thumbor/eT0h9ntj8Vv9oY-WTg3uLcqFZ9c=/1400x1400/filters:format(png)/cdn.vox-cdn.com/uploads/chorus_asset/file/23260648/sf6.png"
      def logoData = None
      def sections = List.empty[org.scalaexercises.runtime.model.Section]
      def timestamp = Instant.now().toString()
      def buildMetaInfo = new org.scalaexercises.runtime.model.BuildInfo {
        def resolvers = Nil
        def libraryDependencies = Nil
      }
    }
  ))

  lazy val (libraries: List[Library], librarySections: Map[String, List[Section]]) = {
    val libraries1 = colorize(runtimeLibraries).map(convertLibrary)
    errors.foreach(error =>
      logger.warn(s"error loading lib: $error")
    ) // TODO: handle errors better?
    (libraries1, libraries1.map(lib => lib.name -> lib.sections).toMap)
  }

  def section(libraryName: String, name: String): Option[Section] =
    librarySections.get(libraryName) >>= (_.find(_.name == name))

  def buildRuntimeInfo(evaluation: ExerciseEvaluation): ExerciseEvaluation.EvaluationRequest = {

    val runtimeInfo: Either[String, (RuntimeBuildInfo, String, List[String])] = for {

      runtimeLibrary <- Either.fromOption(
        runtimeLibraries.find(_.name == evaluation.libraryName),
        s"Unable to find library ${evaluation.libraryName} when " +
          s"attempting to evaluate method ${evaluation.method}"
      )

      runtimeSection <- Either.fromOption(
        runtimeLibrary.sections.find(_.name == evaluation.sectionName),
        s"Unable to find section ${evaluation.sectionName} when " +
          s"attempting to evaluate method ${evaluation.method}"
      )

      runtimeExercise <- Either.fromOption(
        runtimeSection.exercises.find(_.qualifiedMethod == evaluation.method),
        s"Unable to find exercise for method ${evaluation.method}"
      )

    } yield (runtimeLibrary.buildMetaInfo, runtimeExercise.packageName, runtimeExercise.imports)

    runtimeInfo map { case (b: RuntimeBuildInfo, pckgName: String, importList: List[String]) =>
      val (resolvers, dependencies, code) = Exercises.buildEvaluatorRequest(
        pckgName,
        evaluation.method,
        evaluation.args,
        importList,
        b.resolvers,
        b.libraryDependencies
      )

      (
        resolvers,
        dependencies map (d => CoreDependency(d.groupId, d.artifactId, d.version)),
        code
      )

    }
  }

  def reorderLibraries(topLibNames: List[String], libraries: List[Library]): List[Library] = {
    val topLibIndex = topLibNames.zipWithIndex.toMap
    libraries.sortBy(lib => topLibIndex.getOrElse(lib.name, Integer.MAX_VALUE))
  }
}

sealed trait RuntimeSharedConversions {
  // not particularly clean, but this assigns colors
  // to libraries that don't have a default color provided
  // TODO: make this nicer
  def colorize(libraries: List[RuntimeLibrary]): List[RuntimeLibrary] = {
    type Colors = List[String]
    val autoPalette: Colors = List(
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

    libraries
      .traverse { library =>
        if (library.color.isEmpty) {
          State[Colors, RuntimeLibrary] { colors =>
            val (color, colors0) = colors match {
              case head :: tail => Some(head) -> tail
              case Nil          => None       -> Nil
            }
            val library0 = DefaultLibrary(
              owner = library.owner,
              repository = library.repository,
              name = library.name,
              description = library.description,
              color = color,
              logoPath = library.logoPath,
              logoData = library.logoData,
              sections = library.sections,
              timestamp = Timestamp.fromDate(new java.util.Date()),
              buildMetaInfo = library.buildMetaInfo
            )
            (colors0, library0)
          }
        } else
          State.pure[Colors, RuntimeLibrary](library)
      }
      .runA(autoPalette)
      .value
  }

  def convertLibrary(library: RuntimeLibrary): Library =
    Library(
      owner = library.owner,
      repository = library.repository,
      name = library.name,
      description = library.description,
      color = library.color getOrElse "black",
      logoPath = library.logoPath,
      logoData = loadLogoSrc(library.logoPath),
      sections = library.sections map convertSection,
      timestamp = library.timestamp,
      buildInfo = convertBuildInfo(library.buildMetaInfo)
    )

  def loadLogoSrc(logoPath: String): Option[String] = {
    def checkLogoPath(): Boolean =
      Option(getClass.getClassLoader.getResource(logoPath + ".svg")).isDefined

    def logoData(path: String): Option[String] =
      Option(getClass.getClassLoader.getResourceAsStream(path))
        .map(stream => Base64.getMimeEncoder.encodeToString(IOUtils.toByteArray(stream)))

    val logoPathOrDefault =
      if (checkLogoPath()) logoPath + ".svg" else "public/images/library_default_logo.svg"
    logoData(logoPathOrDefault)
  }

  def convertBuildInfo(buildInfo: RuntimeBuildInfo): BuildInfo =
    BuildInfo(
      resolvers = buildInfo.resolvers,
      libraryDependencies = buildInfo.libraryDependencies
    )

  def convertSection(section: RuntimeSection): Section =
    Section(
      name = section.name,
      description = section.description,
      path = section.path,
      exercises = section.exercises.map(convertExercise),
      contributions = section.contributions.map(convertContribution)
    )

  def convertExercise(exercise: RuntimeExercise): Exercise =
    Exercise(
      method = exercise.qualifiedMethod,
      name = Option(exercise.name),
      description = exercise.description,
      code = Option(exercise.code),
      explanation = exercise.explanation
    )

  def convertContribution(contribution: RuntimeContribution): Contribution =
    Contribution(
      sha = contribution.sha,
      message = contribution.message,
      timestamp = contribution.timestamp,
      url = contribution.url,
      author = contribution.author,
      authorUrl = contribution.authorUrl,
      avatarUrl = contribution.avatarUrl
    )

}
