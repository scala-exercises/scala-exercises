package utils

import org.scalajs.dom
import org.scalajs.dom.raw.{HTMLDivElement, HTMLElement, HTMLInputElement}
import org.scalajs.jquery.{ jQuery ⇒ $, JQuery }

object DomHandler {

  val resAssert = """(?s)\((res[0-9]*)\)""".r

  def allExercises: Seq[HTMLElement] = $(".exercise").divs

  def methodsList: Seq[Option[String]] = allExercises map methodName






  def methodName(e: HTMLElement): Option[String] = Option($(e).attr("data-method")).filter(_.trim.nonEmpty)

  def methodParent(input: HTMLInputElement): Option[String] = methodName($(input).closest(".exercise").getDiv)

  def allInputs: Seq[HTMLInputElement] = $(".exercise-code>input").inputs

  def findMethod(method: String): Option[HTMLElement] = allExercises.find(methodName(_) == Option(method))

  def inputsMethod(method: String): Seq[Option[String]] = findMethod(method) match {
    case Some(e) =>  $(e).find("input").inputs map inputValue
    case _ => Nil
  }

  def inputValue(input: HTMLInputElement): Option[String] = Option($(input).value.toString).filter(_.trim.nonEmpty)



  def activeInputs(callback: (Option[String], Seq[Option[String]]) ⇒ Unit) = allInputs.map(input =>
    $(input)
        .width(inputSize($(input).value().toString.length))
        .keyup((e: dom.Event) ⇒ {
          $(input).width(inputSize($(input).value.toString.length))
          val method = methodParent(input)
          callback(method, inputsMethod(method))
        })

  )


  def getCodeBlocks: Seq[HTMLElement] = $("pre code").elements

  def insertInput = getCodeBlocks.map(c => $(c).html(resAssert.replaceAllIn($(c).text(), """(<input type="text" data-res="$1"/>)""")))










  private def inputSize(length: Int) = length match {
    case 0 ⇒ 12d
    case _ ⇒ (12 + (length + 1) * 7).toDouble
  }

  implicit class JQueryOps(j : JQuery) {

    def elements : Seq[HTMLElement] = all[HTMLElement]

    def divs : Seq[HTMLDivElement] = all[HTMLDivElement]

    def inputs : Seq[HTMLInputElement] = all[HTMLInputElement]

    def getDiv: HTMLDivElement = get[HTMLDivElement]

    def all[A <: dom.Element] : Seq[A] = j.toArray().collect {case d : A => d}

    def get[A <: dom.Element] : A = j.get().asInstanceOf[A]

  }

}
