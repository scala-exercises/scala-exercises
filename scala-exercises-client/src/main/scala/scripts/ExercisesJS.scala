package scripts

import config.Routes
import messages.EvaluationRequest
import org.scalajs.dom.ext.{ AjaxException, Ajax }
import common.ExtAjax._
import upickle._
import scala.scalajs.js
import org.scalajs.dom
import org.scalajs.jquery.{ jQuery ⇒ $, JQuery }
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

object ExercisesJS extends js.JSApp {

  val resAssert = """(?s)\((res[0-9]*)\)""".r

  def main(): Unit = {
    insertInputs

  }

  private def insertInputs = {
    $("pre code").each({ (_: Any, code: dom.Element) ⇒
      val lineWithInput = resAssert.replaceAllIn($(code).text(), """(<input type="text" data-res="$1"/>)""")
      $(code).html(lineWithInput)
    })
    runHighlight
  }

  private def runHighlight = {
    $("pre code").each({ (_: Any, code: dom.Element) ⇒
      js.Dynamic.global.hljs.highlightBlock(code)
    })
    activeInputs
  }

  private def activeInputs = {
    $(".exercise-code>input").each({ (_: Any, input: dom.Element) ⇒
      $(input)
        .width(inputSize($(input).value().toString.length))
        .keyup((e: dom.Event) ⇒ $(input).width(inputSize($(input).value().toString.length)))
        .blur((e: dom.Event) ⇒ evalAnswered($(input).closest(".exercise")))
    })
  }

  def evalAnswered(exercise: JQuery) = {
    val inputs = exercise.find("input")
    val filledInputs = inputs.filter({ (index: Int, e: dom.Element) ⇒ isAnswered(e) })

    if (filledInputs.length == inputs.length) evalExercise(exercise)
  }

  def evalExercise(exercise: JQuery) = {
    val section = exercise.attr("data-section")
    val category = exercise.attr("data-category")
    val method = exercise.attr("data-exercise")
    val args = exercise.find("input").toArray().map(e ⇒ $(e.asInstanceOf[dom.Element]).value.toString).toSeq
    val request = EvaluationRequest(section, category, method, args)

    Ajax.postAsJson(Routes.Exercises.evaluate(section, category), write(request)).map { r ⇒
      if (r.responseText.contains("Evaluation succeded")) runSucceded(exercise, request)
      else runFailed(exercise, request)
    }.recover { case e: AjaxException ⇒ runFailed(exercise, request) }
  }

  def runSucceded(exercise: JQuery, request: EvaluationRequest) = {
    exercise.find(".ribbon-wrapper").show()
    exercise.find("input").addClass("success")
  }

  def runFailed(exercise: JQuery, request: EvaluationRequest) = {
    exercise.find(".ribbon-wrapper").hide()
    exercise.find("input").removeClass("success")
  }

  private def isAnswered(e: dom.Element): Boolean = !$(e).value.toString.isEmpty

  private def inputSize(length: Int) = length match {
    case 0 ⇒ 12d
    case _ ⇒ (12 + (length + 1) * 7).toDouble
  }

}
