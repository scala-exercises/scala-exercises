/*
 * scala-exercises-runtime
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises
import cats.data.{ Ior, Xor }
import java.nio.file.Path
import scala.concurrent.duration._

object MethodEval {

  /** An evaluation result, which can have three possible results. */
  sealed abstract class EvaluationResult[A](val didRun: Boolean) extends Product with Serializable {

    /** Converts the result to an Xor with all exceptions projected on the left.
      * The result value is projected on the right.
      */
    def toSuccessXor: Xor[Xor[EvaluationFailure[A], EvaluationException[A]], EvaluationSuccess[A]] =
      this match {
        case ef: EvaluationFailure[A]   ⇒ Xor.left(Xor.left(ef))
        case ee: EvaluationException[A] ⇒ Xor.left(Xor.right(ee))
        case es: EvaluationSuccess[A]   ⇒ Xor.right(es)
      }

    /** Converts the result to an Xor where executions during evaluation are
      * projected on the right. Reflection or compilation exceptions are
      * projected left.
      */
    def toExecutionXor: Xor[EvaluationFailure[A], Xor[EvaluationException[A], EvaluationSuccess[A]]] =
      this match {
        case ef: EvaluationFailure[A]   ⇒ Xor.left(ef)
        case ee: EvaluationException[A] ⇒ Xor.right(Xor.left(ee))
        case es: EvaluationSuccess[A]   ⇒ Xor.right(Xor.right(es))
      }
  }

  /** An evaluation that failed (miersably) due to reflection or compilation errors
    * (including parameter type errors).
    */
  case class EvaluationFailure[A](reason: Ior[String, Throwable]) extends EvaluationResult[A](false) {

    /** Convenience; provides a single exception for the underlying reason. */
    def foldedException: EvaluationFailureException = reason.fold(
      new EvaluationFailureException(_, null),
      new EvaluationFailureException(null, _),
      new EvaluationFailureException(_, _)
    )
  }

  /** A convenince exception that captures why an evaluation failed to run */
  class EvaluationFailureException private[MethodEval] (message: String, e: Throwable) extends Exception(message, e)

  /** An evaluation that ran, but threw an exception. */
  case class EvaluationException[A](e: Throwable) extends EvaluationResult[A](true)

  /** An evaluation that ran and returned a result. */
  case class EvaluationSuccess[A](res: A) extends EvaluationResult[A](true)
}

class MethodEval() {
  import MethodEval._
  import EvalResult._

  val timeout = 20.seconds
  private val evaluator = new Evaluator(timeout)

  def eval[T](pkg: String, qualifiedMethod: String, rawArgs: List[String], imports: List[String] = Nil): EvaluationResult[_] = {
    val pre = (s"import $pkg._" :: imports).mkString(System.lineSeparator)
    val code = s"""$qualifiedMethod(${rawArgs.mkString(", ")})"""

    def convert(runtimeError: Option[RuntimeError]): Throwable = {
      val unknown = new Exception("unknown error")
      runtimeError.map(_.error).getOrElse(unknown)
    }

    // * propagate as much information as possible to the ui
    // * the user provide rawArgs and is wraped inside a class, you want to shift error position
    evaluator[T](pre, code) match {

      case Success(complilationInfos, result, consoleOutput) ⇒
        EvaluationSuccess(result)

      case EvalRuntimeError(complilationInfos, runtimeError) ⇒
        EvaluationException(convert(runtimeError))

      case CompilationError(complilationInfos) ⇒ {
        val messages = complilationInfos.values.flatMap(_.map(_.message)).mkString(", ")
        EvaluationFailure(Ior.left(s"compilation error $messages"))
      }

      case GeneralError(stack) ⇒ {
        EvaluationFailure(Ior.both(stack.getMessage, stack))
      }

      case Timeout ⇒
        EvaluationFailure(Ior.left(s"compilation timed out after $timeout"))
    }
  }
}
