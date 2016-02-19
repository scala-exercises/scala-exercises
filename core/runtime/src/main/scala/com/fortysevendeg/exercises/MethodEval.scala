/*
 * scala-exercises-runtime
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises

import java.lang.reflect.InvocationTargetException
import scala.reflect.runtime.{ universe ⇒ ru }
import scala.reflect.runtime.{ currentMirror ⇒ cm }
import scala.tools.reflect.ToolBox

import cats.data.{ Ior, Xor }
import cats.std.list._
import cats.syntax.flatMap._
import cats.syntax.traverse._

object MethodEval {
  sealed abstract class EvaluationResult extends Product with Serializable {
    def didRun: Boolean
  }

  case class EvaluationFailure(reason: Ior[String, Throwable]) extends EvaluationResult {
    override def didRun = false
  }
  case class EvaluationSuccess[A](res: A) extends EvaluationResult {
    override def didRun = true
  }
  case class EvaluationException(e: Throwable) extends EvaluationResult {
    override def didRun = true
  }
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
  def eval(qualifiedMethod: String, rawArgs: List[String]): EvaluationResult = {
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

    args ← (rawArgs zip argTypes)
      .map { case (arg, tpe) ⇒
        catching(toolbox.parse(arg),
          s"Unable to parse arg $arg") >>= { parsedArg ⇒
        catching(toolbox.eval(Typed(parsedArg, tq"$tpe")),
          s"Unable to evaluate arg $arg as $tpe")
      }}
      .sequenceU

    result ←
      try {
        success(EvaluationSuccess(instanceMirror.reflectMethod(methodSymbol)(args: _*)))
      } catch {
        // capture exceptions thrown by the method, since they are considered success
        case e: InvocationTargetException => success(EvaluationException(e.getCause))
        case scala.util.control.NonFatal(t) => error(t)
      }
  } yield result

  result.fold(
    EvaluationFailure(_),
    identity
  )
}

  // format: ON
}
