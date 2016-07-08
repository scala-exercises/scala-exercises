/*
 * scala-exercises-runtime
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */
package org.scalaexercises.runtime

import java.io.File
import java.nio.file.Path
import java.util.jar.JarFile
import scala.tools.nsc.Settings

import scala.tools.nsc.{ Global, Settings }
import scala.tools.nsc.reporters._
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
import com.twitter.util.Eval

import scala.reflect.internal.util.{ BatchSourceFile, Position }

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

  def convert(errors: (Position, String, String)): (Severity, List[CompilationInfo]) = {
    val (pos, msg, severity) = errors
    val sev = severity match {
      case "ERROR"   ⇒ Error
      case "WARNING" ⇒ Warning
      case _         ⇒ Info
    }
    (sev, CompilationInfo(msg, Some(RangePosition(pos.start, pos.point, pos.end))) :: Nil)
  }

  def apply[T](pre: String, code: String): EvalResult[T] = {
    val allCode = s"""
                     |$pre
                     |$code
                   """.stripMargin
    val eval = new Eval {
      @volatile var errors: Map[Severity, List[CompilationInfo]] = Map.empty

      override lazy val compilerMessageHandler: Option[Reporter] = Some(new AbstractReporter {
        override val settings: Settings = compilerSettings
        override def displayPrompt(): Unit = ()
        override def display(pos: Position, msg: String, severity: this.type#Severity): Unit = {
          errors += convert((pos, msg, severity.toString))
        }
        override def reset() = {
          super.reset()
          errors = Map.empty
        }
      })
    }

    val result = for {
      _ ← Try(eval.check(allCode))
      result ← Try(eval.apply[T](allCode, resetState = true))
    } yield result

    val errors: Map[Severity, List[CompilationInfo]] = eval.errors.toMap

    println(allCode)

    println(result)

    println(errors)

    result match {
      case scala.util.Success(r) ⇒ EvalResult.Success[T](errors, r, "")
      case scala.util.Failure(t) ⇒ t match {
        case e: Eval.CompilerException ⇒ EvalResult.CompilationError(errors)
        case e                         ⇒ EvalResult.EvalRuntimeError(errors, None)
      }
    }

  }

}
