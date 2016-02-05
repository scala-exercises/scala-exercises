package com.fortysevendeg.exercises
package compiler

import scala.tools.nsc._
import scala.tools.nsc.doc.{ Settings ⇒ _, _ }

/** Scala compiler global needed for extracting doc comments. This uses the
  * ScaladocSyntaxAnalyzer, which keeps DocDefs in the parsed AST.
  *
  * It would be ideal to do this as a compiler plugin. Unfortunately there
  * doesn't seem to be a way to replace the syntax analyzer phase (named
  * "parser") with a plugin.
  */
class DocExtractionGlobal(settings: Settings = DocExtractionGlobal.defaultSettings) extends Global(settings) {

  override lazy val syntaxAnalyzer = new ScaladocSyntaxAnalyzer[this.type](this) {
    val runsAfter = List[String]()
    val runsRightAfter = None
    override val initial = true
  }

  override def newUnitParser(unit: CompilationUnit) = new syntaxAnalyzer.ScaladocUnitParser(unit, Nil)

  override protected def computeInternalPhases() {
    phasesSet += syntaxAnalyzer
    phasesSet += analyzer.namerFactory
  }

}

object DocExtractionGlobal {
  def defaultSettings = new Settings {
    embeddedDefaults[DocExtractionGlobal.type]
  }
}
