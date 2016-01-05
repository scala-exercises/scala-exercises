package utils

import org.scalajs.dom
import org.scalajs.jquery.{ jQuery ⇒ $, JQuery }

object DomHandler {

  val resAssert = """(?s)\((res[0-9]*)\)""".r

  def allExercises: JQuery = $(".exercise")

  def methodsList: Seq[String] = allExercises.toArray().map(e ⇒ methodName(e.asInstanceOf[dom.Element]))

  def methodName(e: dom.Element): String = $(e).attr("data-method")

  def methodParent(input: JQuery): String = methodName(input.closest(".exercise").get().asInstanceOf[dom.Element])

  def allInputs: JQuery = $(".exercise-code>input")

  def inputsMethod(method: String): Seq[String] = inputsValues(allExercises.filter((index: Int, e: dom.Element) ⇒ methodName(e) == method).find("input"))

  def inputsValues(inputs: JQuery): Seq[String] = inputs.toArray().map(e ⇒ $(e.asInstanceOf[dom.Element]).value.toString)

  def activeInputs(callback: (String, Seq[String]) ⇒ Unit): JQuery = allInputs.each({ (_: Any, input: dom.Element) ⇒
    $(input)
      .width(inputSize($(input).value().toString.length))
      .keyup((e: dom.Event) ⇒ {
        $(input).width(inputSize($(input).value().toString.length))
        val method = methodParent($(input))
        callback(method, inputsMethod(method))
      })
  })

  def insertInputs: JQuery = $("pre code").each({ (_: Any, code: dom.Element) ⇒
    $(code).html(resAssert.replaceAllIn($(code).text(), """(<input type="text" data-res="$1"/>)"""))
  })

  private def inputSize(length: Int) = length match {
    case 0 ⇒ 12d
    case _ ⇒ (12 + (length + 1) * 7).toDouble
  }
}
