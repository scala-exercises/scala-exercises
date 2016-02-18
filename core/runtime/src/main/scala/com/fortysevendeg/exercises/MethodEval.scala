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

class MethodEval {

  private[this] val toolbox = cm.mkToolBox()
  private[this] val mirror = ru.runtimeMirror(classOf[MethodEval].getClassLoader)
  import mirror.universe._

  type Res[A] = Xor[Ior[String, Throwable], A]

  private[this] object Res {
    def error[A](message: String): Res[A] = Xor.left(Ior.left(message))
    def error[A](e: Throwable): Res[A] = Xor.left(Ior.right(e))
    def error[A](message: String, e: Throwable): Res[A] = Xor.left(Ior.both(message, e))
    def success[A](value: A): Res[A] = Xor.right(value)

    def catching[A](f: ⇒ A): Res[A] = Xor.catchNonFatal(f).leftMap(e ⇒ Ior.right(e))
    def catching[A](f: ⇒ A, message: ⇒ String): Res[A] = Xor.catchNonFatal(f).leftMap(e ⇒ Ior.both(message, e))
  }

  import Res._

  // format: OFF
  def eval(qualifiedMethod: String, rawArgs: List[String]): Res[Xor[Throwable, Any]] = for {

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
        success(Xor.right(instanceMirror.reflectMethod(methodSymbol)(args: _*)))
      } catch {
        // capture exceptions thrown by the method, since they are considered success
        case e: InvocationTargetException => success(Xor.left(e.getCause))
        case scala.util.control.NonFatal(t) => error(t)
      }


  } yield result

  // format: ON
}
