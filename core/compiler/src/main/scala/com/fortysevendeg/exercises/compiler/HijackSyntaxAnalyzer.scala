/*
 * scala-exercises-exercise-compiler
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises
package compiler

import scala.tools.nsc.{ Global ⇒ NscGlobal, SubComponent, Phase }
import scala.tools.nsc.plugins.{ Plugin ⇒ NscPlugin }
import scala.tools.nsc.ast.parser.{ SyntaxAnalyzer ⇒ NscSyntaxAnalyzer }
import scala.tools.nsc.doc.ScaladocSyntaxAnalyzer

import scala.tools.nsc.doc.ScaladocGlobalTrait

class ExercisePlugin(val global: NscGlobal) extends NscPlugin with PhaseJacking {
  override val name = "exercise-compiler"
  override val description = "the scala exercise compiler plugin"

  override val components = Nil

  val newSyntaxAnalyzer = new ScaladocSyntaxAnalyzer[global.type](global) {
    val runsAfter = List[String]()
    val runsRightAfter = None
    override val initial = true

    import global._

    // a lot of this is ripped... all so that we can override this method
    def newUnitParser(unit: CompilationUnit) = new ScaladocUnitParser(unit, Nil)

    // ripped
    override def newPhase(prev: Phase): StdPhase = new ParserPhase(prev)

    // ripped
    private def initialUnitBody(unit: CompilationUnit): Tree = {
      if (unit.isJava) new JavaUnitParser(unit).parse()
      else if (currentRun.parsing.incompleteHandled) newUnitParser(unit).parse()
      else newUnitParser(unit).smartParse()
    }

    // ripped
    class ParserPhase(prev: Phase) extends StdPhase(prev) {
      override val checkable = false
      override val keepsTypeParams = false
      def apply(unit: CompilationUnit) {
        informProgress("parsing " + unit)
        if (unit.body == EmptyTree) unit.body = initialUnitBody(unit)
        if (settings.Yrangepos && !reporter.hasErrors) validatePositions(unit.body)
        if (settings.Ymemberpos.isSetByUser)
          new MemberPosReporter(unit) show (style = settings.Ymemberpos.value)
      }
    }

  }

  hijackField("syntaxAnalyzer", newSyntaxAnalyzer)
  hijackPhase("parser", newSyntaxAnalyzer)

  if (global.syntaxAnalyzer != newSyntaxAnalyzer) sys.error("failed to hijack parser")
}

/** Hijacking compiler phases.
  *
  * I originally went down this route... and got it half way there.
  * Fortunately I stumbled on Vladimir Nikolaev's work (for TASTY scalac support,
  * and scala.meta hosted), which took it a bit further.
  */
sealed trait PhaseJacking { self: NscPlugin ⇒

  import scala.collection.mutable

  private lazy val globalClass: Class[_] = classOf[NscGlobal]

  def hijackField[T](name: String, newValue: T): T = {
    val field = globalClass.getDeclaredField(name)
    field.setAccessible(true)
    val oldValue = field.get(global).asInstanceOf[T]
    field.set(global, newValue)
    oldValue
  }

  private lazy val phasesSetMapGetter = classOf[NscGlobal].getDeclaredMethod("phasesSet")
  private lazy val phasesSet = phasesSetMapGetter.invoke(global).asInstanceOf[mutable.Set[SubComponent]]

  def hijackPhase(name: String, newPhase: SubComponent): Option[SubComponent] =
    phasesSet.find(_.phaseName == name).map { oldPhase ⇒
      phasesSet -= oldPhase
      phasesSet += newPhase
      oldPhase
    }

}
