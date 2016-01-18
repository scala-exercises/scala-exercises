package actions

import model.Exercises._

case class EvaluationResult(method: String, result: String)

sealed trait Action
case class NoOp() extends Action
case class SetState(s: State) extends Action
case class UpdateExercise(method: String, args: Seq[String]) extends Action
case class CompileExercise(method: String) extends Action
case class CompilationResult(result: EvaluationResult) extends Action
