/*
 * scala-exercises-runtime
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises

import scala.reflect.runtime.{ universe ⇒ ru }
import scala.reflect.runtime.{ currentMirror ⇒ cm }
import scala.tools.reflect.ToolBox

import cats.data.{ Ior, Xor }
import cats.std.list._
import cats.syntax.flatMap._
import cats.syntax.traverse._

class MethodEval {

  type Err = Ior[String, Throwable]

  private val toolbox = cm.mkToolBox()
  private val mirror = ru.runtimeMirror(classOf[MethodEval].getClassLoader)
  import mirror.universe._

  private def catching[A](f: ⇒ A): Xor[Err, A] =
    Xor.catchNonFatal(f).leftMap(e ⇒ Ior.right(e))
  private def catching[A](f: ⇒ A, msg: ⇒ String): Xor[Err, A] =
    Xor.catchNonFatal(f).leftMap(e ⇒ Ior.both(msg, e))

  def eval(qualifiedMethod: String, rawArgs: List[String]): Xor[Err, Any] = {
    val lastIndex = qualifiedMethod.lastIndexOf('.')
    if (lastIndex > 0) {
      val moduleName = qualifiedMethod.substring(0, lastIndex)
      val methodName = qualifiedMethod.substring(lastIndex + 1)
      for {
        staticModule ← catching(mirror.staticModule(moduleName), s"Unable to load module $moduleName")
        moduleMirror ← catching(mirror.reflectModule(staticModule), s"Unable to reflect module mirror for $moduleName")
        instanceMirror ← catching(mirror.reflect(moduleMirror.instance), s"Unable to reflect instance mirror for $moduleName")
        methodSymbol ← catching(
          instanceMirror.symbol.typeSignature.decl(TermName(methodName)).asMethod, s"Unable to get type for module $moduleName"
        )
        argTypes ← methodSymbol.paramLists match {
          case singleGroup :: Nil ⇒ Xor.right(singleGroup.map(_.typeSignature))
          case _                  ⇒ Xor.left(Ior.left(s"Expected just one argument group on method $qualifiedMethod"))
        }
        args ← (rawArgs zip argTypes).map(arg ⇒ catching(toolbox.parse(arg._1), s"Unable to parse arg ${arg._1}") >>= { tree ⇒
          catching(toolbox.eval(Typed(tree, tq"${arg._2}")), s"Unable to evaluate arg ${arg._1} as ${arg._2}")
        }).sequenceU
        result ← catching(instanceMirror.reflectMethod(methodSymbol)(args: _*), s"Error while calling $qualifiedMethod")
      } yield result
    } else Xor.left(Ior.left(s"Invalid qualified method $qualifiedMethod"))
  }

}
