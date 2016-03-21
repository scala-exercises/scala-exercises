/*
 * scala-exercises-runtime
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises

import scala.language.experimental.macros

import java.lang.reflect.InvocationTargetException
import scala.reflect.runtime.{ universe ⇒ ru }
import scala.reflect.runtime.{ currentMirror ⇒ cm }
import scala.tools.reflect.ToolBox

import cats.data.{ Ior, Xor }
import cats.std.list._
import cats.syntax.flatMap._
import cats.syntax.traverse._

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

class MethodEval {
  import MethodEval._

  private[this] val toolbox = cm.mkToolBox()
  private[this] val mirror = ru.runtimeMirror(classOf[MethodEval].getClassLoader)
  import mirror.universe._

  private[this]type Res[A] = Xor[Ior[String, Throwable], A]

  private[this] def error[A](message: String): Res[A] = Xor.left(Ior.left(message))
  private[this] def error[A](e: Throwable): Res[A] = Xor.left(Ior.right(e))
  private[this] def error[A](message: String, e: Throwable): Res[A] = Xor.left(Ior.both(message, e))
  private[this] def success[A](value: A): Res[A] = Xor.right(value)

  private[this] def catching[A](f: ⇒ A): Res[A] = Xor.catchNonFatal(f).leftMap(e ⇒ Ior.right(e))
  private[this] def catching[A](f: ⇒ A, message: ⇒ String): Res[A] = Xor.catchNonFatal(f).leftMap(e ⇒ Ior.both(message, e))


  // format: OFF
  def eval(qualifiedMethod: String, rawArgs: List[String], imports: List[String] = Nil): EvaluationResult[_] = {
    val result = for {

    lastIndex ← {
      val lastIndex = qualifiedMethod.lastIndexOf('.')
      if (lastIndex > 0) success(lastIndex)
      else error(s"Invalid qualified method $qualifiedMethod")
    }
    moduleName = qualifiedMethod.substring(0, lastIndex)
    methodName = qualifiedMethod.substring(lastIndex + 1)

    staticModule ← catching(mirror.staticModule(moduleName),
      s"Unable to load module $moduleName")

    moduleMirror ← catching(mirror.reflectModule(staticModule),
      s"Unable to reflect module mirror for $moduleName")

    instanceMirror ← catching(mirror.reflect(moduleMirror.instance),
      s"Unable to reflect instance mirror for $moduleName")

    methodSymbol ← catching(instanceMirror.symbol.typeSignature.decl(TermName(methodName)).asMethod,
      s"Unable to get type for module $moduleName")

    argTypes ← methodSymbol.paramLists match {
      case singleGroup :: Nil ⇒ success(singleGroup.map(_.typeSignature))
      case _                  ⇒ error(s"Expected just one argument group on method $qualifiedMethod")
    }


    importer0 = ru.mkImporter(mirror.universe)
    importer = importer0.asInstanceOf[ru.Importer { val from: mirror.universe.type }]

    args ← (rawArgs zip argTypes).map { case (arg, tpe) ⇒
        for {
          parsedArg ← catching(toolbox.parse(arg),
            s"Unable to parse arg $arg")

          parsedImports ← imports.map(imp ⇒ catching(toolbox.parse(imp),
            s"Unable to parse import '$imp'")).sequenceU

          tree = q"""
            ..$parsedImports
            $parsedArg: $tpe
          """

          evaledArg ← catching(toolbox.eval(tree),
              s"Unable to evaluate arg $arg as $tpe")

        } yield evaledArg

      }.sequenceU

    result ←
      try {
        success(EvaluationSuccess(instanceMirror.reflectMethod(methodSymbol)(args: _*)))
      } catch {
        // capture exceptions thrown by the method, since they are considered success
        case e: InvocationTargetException ⇒ success(EvaluationException(e.getCause))
        case scala.util.control.NonFatal(t) ⇒ error(t)
      }
  } yield result

  result.fold(
    EvaluationFailure(_),
    identity
  )
}

  // format: ON
}
