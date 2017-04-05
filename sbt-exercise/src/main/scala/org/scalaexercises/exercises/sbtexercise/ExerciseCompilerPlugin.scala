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

package org.scalaexercises.plugin
package sbtexercise

import scala.language.implicitConversions
import scala.language.reflectiveCalls
import scala.collection.mutable.ArrayBuffer
import java.io.File
import java.io.OutputStream
import java.io.PrintStream
import scala.language.reflectiveCalls

import sbt.{`package` ⇒ _, _}
import sbt.Keys._
import sbt.classpath.ClasspathUtilities
import xsbt.api.Discovery
import cats.{`package` ⇒ _}
import cats.data.Ior
import cats.implicits._
import sbtbuildinfo.BuildInfoPlugin
import sbtbuildinfo.BuildInfoPlugin.autoImport._

/** The exercise compiler SBT auto plugin */
object ExerciseCompilerPlugin extends AutoPlugin {

  // this plugin requires the JvmPlugin and must be
  // explicitly enabled by projects
  override def requires = plugins.JvmPlugin && BuildInfoPlugin
  override def trigger  = noTrigger

  lazy val CompileGeneratedExercises = config("compile-generated-exercises")
  lazy val CustomCompile             = config("compile") extend CompileGeneratedExercises

  val generateExercises = TaskKey[List[(String, String)]]("generate-exercises")

  val fetchContributors =
    SettingKey[Boolean]("fetch-contributors", "Fetch contributors when compiling")

  object autoImport {
    def CompileGeneratedExercises = ExerciseCompilerPlugin.CompileGeneratedExercises
  }

  // format: OFF
  override def projectSettings =
    inConfig(CompileGeneratedExercises)(
      Defaults.compileSettings ++
      Defaults.compileInputsSettings ++
      Defaults.configTasks ++
      redirSettings ++ Seq(

      products := {
        products.value ++
        (products in Compile).value
      },

      sourceGenerators   <+= generateExerciseSourcesTask,
      generateExercises  <<= generateExercisesTask
    )) ++
    Seq(
      compile        := (compile in CompileGeneratedExercises).value,
      copyResources  := (copyResources in CompileGeneratedExercises).value,
      `package`      := (`package` in CompileGeneratedExercises).value,
      artifacts    <++= Classpaths.artifactDefs(Seq(packageBin in CompileGeneratedExercises)),
      artifact in (CompileGeneratedExercises, packageBin) ~= { (art: Artifact) =>
        art.copy(classifier = None)
      },
      ivyConfigurations   :=
        overrideConfigs(CompileGeneratedExercises, CustomCompile)(ivyConfigurations.value),
      classDirectory in CompileGeneratedExercises := (classDirectory in Compile).value,
      classpathConfiguration in CompileGeneratedExercises := (classpathConfiguration in Compile).value,
      fetchContributors := true,
      buildInfoObject   := "LibMetaInfo",
      buildInfoPackage  := "org.scalaexercises.content",
      buildInfoKeys     := Seq(
        name,
        version,
        scalaVersion,
        organization,
        resolvers,
        libraryDependencies
      ),
      buildInfoOptions += BuildInfoOption.Traits("org.scalaexercises.definitions.BuildInfo")
  )
  // format: ON

  def redirSettings = Seq(
    // v0.13.9/main/src/main/scala/sbt/Defaults.scala
    sourceDirectory <<= reconfigureSub(sourceDirectory),
    sourceManaged <<= reconfigureSub(sourceManaged),
    resourceManaged <<= reconfigureSub(resourceManaged)
  )

  /** Helper to facilitate changing the directories. By default, a configuration
   * inheriting from Compile will compile source in
   * `src/<configuration_name>/[scala|test|...]`. This forces the directory
   * back to `src/main/[scala|test|...]`.
   */
  private def reconfigureSub(key: SettingKey[File]): Def.Initialize[File] =
    (key in ThisScope.copy(config = Global), configuration) { (src, conf) ⇒
      src / "main"
    }

  // for most of the work below, a captured error is an error message and/or a
  // throwable value
  private type Err = Ior[String, Throwable]
  private implicit def errFromString(message: String)         = Ior.left(message)
  private implicit def errFromThrowable(throwable: Throwable) = Ior.right(throwable)

  private def catching[A](f: ⇒ A)(msg: ⇒ String) =
    Either.catchNonFatal(f).leftMap(e ⇒ Ior.both(msg, e))

  /** Given an Analysis output from a compile run, this will
   * identify all modules implementing `exercise.Library`.
   */
  private def discoverLibraries(analysis: inc.Analysis): Seq[String] =
    Discovery(Set("org.scalaexercises.definitions.Library"), Set.empty)(Tests.allDefs(analysis))
      .collect({
        case (definition, discovered) if !discovered.isEmpty ⇒ definition.name
      })
      .sorted

  private def discoverSections(analysis: inc.Analysis): Seq[String] =
    Discovery(Set("org.scalaexercises.definitions.Section"), Set.empty)(Tests.allDefs(analysis))
      .collect({
        case (definition, discovered) if !discovered.isEmpty ⇒ definition.name
      })
      .sorted

  // reflection is used to invoke a java-style interface to the exercise compiler
  private val COMPILER_CLASS = "org.scalaexercises.compiler.CompilerJava"
  private type COMPILER = {
    def compile(
        library: AnyRef,
        sources: Array[String],
        paths: Array[String],
        buildMetaInfo: AnyRef,
        baseDir: String,
        targetPackage: String,
        fetchContributors: Boolean
    ): Array[String]
  }

  // worker task that invokes the exercise compiler
  def generateExercisesTask = Def.task {
    val log             = streams.value.log
    val baseDir         = (baseDirectory in Compile).value
    lazy val analysisIn = (compile in Compile).value

    lazy val libraryNames = discoverLibraries(analysisIn)
    lazy val sectionNames = discoverSections(analysisIn)

    val libraryClasspath = Attributed.data((fullClasspath in Compile).value)
    val classpath        = (Meta.compilerClasspath ++ libraryClasspath).distinct
    val loader = ClasspathUtilities.toLoader(
      classpath,
      null,
      ClasspathUtilities.createClasspathResources(
        appPaths = Meta.compilerClasspath,
        bootPaths = scalaInstance.value.jars
      )
    )

    val libMetaInfoClass = catching(
      loader
        .loadClass("org.scalaexercises.content.LibMetaInfo$")
    )("Unable to find LibMetaInfo class")
      .fold({
        case Ior.Left(message) ⇒ throw new Exception(message)
        case Ior.Right(error)  ⇒ throw error
        case Ior.Both(message, error) ⇒
          log.error(message)
          throw error
      }, identity)

    val metaInfo = libMetaInfoClass
      .getField("MODULE$")
      .get(libMetaInfoClass)

    def loadLibraryModule(name: String) =
      for {
        loadedClass ← catching(loader.loadClass(name + "$"))(s"$name not found")
        loadedModule ← catching(loadedClass.getField("MODULE$").get(null))(
          s"$name must be defined as an object"
        )
      } yield loadedModule

    def invokeCompiler(
        compiler: COMPILER,
        library: AnyRef
    ): Either[Err, (String, String)] =
      Either.catchNonFatal {
        val sourceCodes = (libraryNames ++ sectionNames).distinct
          .flatMap(analysisIn.relations.definesClass)
          .map(file ⇒ (file.getPath, IO.read(file)))

        captureStdStreams(
          fOut = log.info(_: String),
          fErr = log.error(_: String)
        ) {
          compiler
            .compile(
              library = library,
              sources = sourceCodes.map(_._2).toArray,
              paths = sourceCodes.map(_._1).toArray,
              buildMetaInfo = metaInfo,
              baseDir = baseDir.getPath,
              targetPackage = "org.scalaexercises.content",
              fetchContributors = fetchContributors.value
            )
            .toList
        }
      } leftMap (e ⇒ e: Err) >>= {
        case mn :: moduleSource :: Nil ⇒
          Right(mn → moduleSource)
        case _ ⇒
          Left("Unexpected return value from exercise compiler": Err)
      }

    val result = for {
      compilerClass ← catching(loader.loadClass(COMPILER_CLASS))(
        "Unable to find exercise compiler class"
      )
      compiler ← catching(compilerClass.newInstance.asInstanceOf[COMPILER])(
        "Unable to create instance of exercise compiler"
      )
      libraries ← libraryNames.toList.traverseU(loadLibraryModule)
      result    ← libraries.traverseU(invokeCompiler(compiler, _))
    } yield result

    result.fold({
      case Ior.Left(message) ⇒ throw new Exception(message)
      case Ior.Right(error)  ⇒ throw error
      case Ior.Both(message, error) ⇒
        log.error(message)
        throw error
    }, identity)
  }

  // task responsible for outputting the source files
  def generateExerciseSourcesTask = Def.task {
    val log = streams.value.log

    val generated = generateExercises.value

    val dir = (sourceManaged in Compile).value
    generated.map {
      case (n, code) ⇒
        val file = dir / (n.replace(".", "/") + ".scala")
        IO.write(file, code)
        log.info(s"Generated library at $file")
        file
    }
  }

  private[this] def captureStdStreams[T](
      fOut: (String) ⇒ Unit,
      fErr: (String) ⇒ Unit
  )(thunk: ⇒ T): T = {
    val originalOut = System.out
    val originalErr = System.err
    System.setOut(new PrintStream(new LineByLineOutputStream(fOut), true))
    System.setErr(new PrintStream(new LineByLineOutputStream(fErr), true))
    val res: T = thunk
    System.setOut(originalOut)
    System.setErr(originalErr)
    res
  }

  /** Output stream that captures an output on a line by line basis.
   *
   * @param f the function to invoke with each line
   */
  private[this] class LineByLineOutputStream(
      f: (String) ⇒ Unit
  ) extends OutputStream {
    val ls                     = System.getProperty("line.separator")
    val buf                    = new ArrayBuffer[Byte](200)
    override def write(b: Int) = if (b != 0) buf += b.toByte
    override def flush() {
      if (buf.nonEmpty) {
        val message = new String(buf.toArray)
        buf.clear()
        if (message != ls) f(message)
      }
    }
  }

}
