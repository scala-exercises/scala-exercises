/*
 * scala-exercises-runtime
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */
package org.scalaexercises.exercises

import java.io.File
import java.nio.file.Path
import java.util.jar.JarFile
import scala.tools.nsc.Settings

import scala.tools.nsc.{ Global, Settings }
import scala.tools.nsc.reporters.StoreReporter
import scala.tools.nsc.io.{ VirtualDirectory, AbstractFile }
import scala.reflect.internal.util.{ NoPosition, BatchSourceFile, AbstractFileClassLoader }

import java.io.File
import java.nio.file.Path
import java.net.{ URL, URLClassLoader }
import java.util.concurrent.{ TimeoutException, Callable, FutureTask, TimeUnit }

import scala.util.Try
import scala.util.control.NonFatal
import scala.concurrent.duration._
import scala.language.reflectiveCalls

sealed trait Severity
final case object Info extends Severity
final case object Warning extends Severity
final case object Error extends Severity

case class RangePosition(start: Int, point: Int, end: Int)
case class CompilationInfo(message: String, pos: Option[RangePosition])
case class RuntimeError(val error: Throwable, position: Option[Int])

sealed trait EvalResult[+T]
object EvalResult {
  type CI = Map[Severity, List[CompilationInfo]]

  case object Timeout extends EvalResult[Nothing]
  case class Success[T](complilationInfos: CI, result: T, consoleOutput: String) extends EvalResult[T]
  case class EvalRuntimeError(complilationInfos: CI, runtimeError: Option[RuntimeError]) extends EvalResult[Nothing]
  case class CompilationError(complilationInfos: CI) extends EvalResult[Nothing]
  case class GeneralError(stack: Throwable) extends EvalResult[Nothing]
}

class Evaluator(timeout: Duration = 20.seconds) {
  val security = false

  private def classPathOfClass(className: String) = {
    val resource = className.split('.').mkString("/", "/", ".class")
    val path = getClass.getResource(resource).getPath
    if (path.indexOf("file:") >= 0) {
      val indexOfFile = path.indexOf("file:") + 5
      val indexOfSeparator = path.lastIndexOf('!')
      List(path.substring(indexOfFile, indexOfSeparator))
    } else {
      require(path.endsWith(resource))
      List(path.substring(0, path.length - resource.length + 1))
    }
  }

  private val compilerPath = try {
    classPathOfClass("scala.tools.nsc.Interpreter")
  } catch {
    case e: Throwable ⇒
      throw new RuntimeException("Unable to load Scala interpreter from classpath (scala-compiler jar is missing?)", e)
  }

  private val libPath = try {
    classPathOfClass("scala.AnyVal")
  } catch {
    case e: Throwable ⇒
      throw new RuntimeException("Unable to load scala base object from classpath (scala-library jar is missing?)", e)
  }

  private val impliedClassPath: List[String] = {
    def getClassPath(cl: ClassLoader, acc: List[List[String]] = List.empty): List[List[String]] = {
      val cp = cl match {
        case urlClassLoader: URLClassLoader ⇒ urlClassLoader.getURLs.filter(_.getProtocol == "file").
          map(u ⇒ new File(u.toURI).getPath).toList
        case _ ⇒ Nil
      }
      cl.getParent match {
        case null   ⇒ (cp :: acc).reverse
        case parent ⇒ getClassPath(parent, cp :: acc)
      }
    }

    val classPath = getClassPath(this.getClass.getClassLoader)
    val currentClassPath = classPath.head

    // if there's just one thing in the classpath, and it's a jar, assume an executable jar.
    currentClassPath ::: (if (currentClassPath.size == 1 && currentClassPath(0).endsWith(".jar")) {
      val jarFile = currentClassPath(0)
      val relativeRoot = new File(jarFile).getParentFile()
      val nestedClassPath = new JarFile(jarFile).getManifest.getMainAttributes.getValue("Class-Path")
      if (nestedClassPath eq null) {
        Nil
      } else {
        nestedClassPath.split(" ").map { f ⇒ new File(relativeRoot, f).getAbsolutePath }.toList
      }
    } else {
      Nil
    }) ::: classPath.tail.flatten
  }

  def apply[T](pre: String, code: String): EvalResult[T] = {
    try { runTimeout[T](pre, code) }
    catch { case NonFatal(e) ⇒ EvalResult.GeneralError(e) }
  }

  private def runTimeout[T](pre: String, code: String) =
    withTimeout { eval[T](pre, code) }(timeout).getOrElse(EvalResult.Timeout)

  private def eval[T](pre: String, code: String): EvalResult[T] = {
    val className = "Eval" + Math.abs(scala.util.Random.nextLong).toString
    val wrapedCode =
      s"""|$pre
          |class $className extends (() ⇒ Any) {
          |  def apply() = $code
          |}""".stripMargin

    secured { compile(wrapedCode) }

    val complilationInfos = check()
    if (!complilationInfos.contains(Error)) {
      try {
        val (result, consoleOutput) = run[T](className)
        EvalResult.Success(complilationInfos, result, consoleOutput)
      } catch { case NonFatal(e) ⇒ EvalResult.EvalRuntimeError(complilationInfos, handleException(e)) }
    } else {
      EvalResult.CompilationError(complilationInfos)
    }
  }

  private def run[T](className: String) = {
    val cl = Class.forName(className, false, classLoader)
    val cons = cl.getConstructor()
    secured {
      val baos = new java.io.ByteArrayOutputStream()
      val ps = new java.io.PrintStream(baos)
      val result = Console.withOut(ps)(cons.newInstance().asInstanceOf[() ⇒ Any].apply().asInstanceOf[T])
      (result, baos.toString("UTF-8"))
    }
  }

  private def withTimeout[T](f: ⇒ T)(timeout: Duration): Option[T] = {
    val task = new FutureTask(new Callable[T]() { def call = f })
    val thread = new Thread(task)
    try {
      thread.start()
      Some(task.get(timeout.toMillis, TimeUnit.MILLISECONDS))
    } catch {
      case e: TimeoutException ⇒ None
    } finally {
      if (thread.isAlive) thread.stop()
    }
  }

  private def handleException(e: Throwable): Option[RuntimeError] = {
    def search(e: Throwable) = {
      e.getStackTrace.find(_.getFileName == "(inline)").map(v ⇒
        (e, Some(v.getLineNumber)))
    }
    def loop(e: Throwable): Option[(Throwable, Option[Int])] = {
      val s = search(e)
      if (s.isEmpty)
        if (e.getCause != null) loop(e.getCause)
        else Some((e, None))
      else s
    }
    loop(e).map {
      case (err, line) ⇒
        RuntimeError(err, line)
    }
  }

  private def check(): Map[Severity, List[CompilationInfo]] = {
    val infos =
      reporter.infos.map { info ⇒
        val pos = info.pos match {
          case NoPosition ⇒ None
          case _          ⇒ Some(RangePosition(info.pos.start, info.pos.point, info.pos.end))
        }
        (
          info.severity,
          info.msg,
          pos
        )
      }.to[List]
        .groupBy(_._1)
        .mapValues { _.map { case (_, msg, pos) ⇒ (msg, pos) } }

    def convert(infos: Map[reporter.Severity, List[(String, Option[RangePosition])]]): Map[Severity, List[CompilationInfo]] = {
      infos.map {
        case (k, vs) ⇒
          val sev = k match {
            case reporter.ERROR   ⇒ Error
            case reporter.WARNING ⇒ Warning
            case reporter.INFO    ⇒ Info
          }
          val info = vs map {
            case (msg, pos) ⇒
              CompilationInfo(msg, pos)
          }
          (sev, info)
      }
    }
    convert(infos)
  }

  private def reset(): Unit = {
    target.clear()
    reporter.reset()
    classLoader = new AbstractFileClassLoader(target, artifactLoader)
  }

  private def compile(code: String): Unit = {
    reset()
    val run = new compiler.Run
    val sourceFiles = List(new BatchSourceFile("(inline)", code))
    run.compileSources(sourceFiles)
  }

  private val reporter = new StoreReporter()

  val settings = new Settings()
  settings.processArguments(List(
    "-deprecation",
    "-encoding", "UTF-8",
    "-unchecked"
  ), true)
  val classpath = (compilerPath ::: libPath ::: impliedClassPath).mkString(File.pathSeparator)
  settings.bootclasspath.value = classpath
  settings.classpath.value = classpath

  private val artifactLoader = {
    val loaderFiles =
      settings.classpath.value.split(File.pathSeparator).map(a ⇒ {
        val node = new java.io.File(a)
        val endSlashed =
          if (node.isDirectory) node.toString + File.separator
          else node.toString

        new File(endSlashed).toURI().toURL
      })
    new URLClassLoader(loaderFiles, this.getClass.getClassLoader)
  }
  private val target = new VirtualDirectory("(memory)", None)
  settings.outputDirs.setSingleOutput(target)
  private var classLoader: AbstractFileClassLoader = _
  private val compiler = new Global(settings, reporter)
  private val secured = new Secured(security)
}