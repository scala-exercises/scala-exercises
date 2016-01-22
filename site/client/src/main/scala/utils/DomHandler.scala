package utils

import org.scalajs.dom
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.raw.{ HTMLDivElement, HTMLElement, HTMLInputElement }
import org.scalajs.jquery.{ jQuery ⇒ $, JQuery }
import shared.IO

import cats.data.OptionT
import cats.syntax.option._

object DomHandler {

  import IO._

  /** Replaces text matched into html inputs
    */
  def replaceInputs(nodes: Seq[(HTMLElement, String)]): IO[Unit] = io {
    nodes foreach { case (n, r) ⇒ $(n).html(r) }
  }

  /** Assigns behaviors to the keyup event for inputs elements.
    */
  def onInputKeyUp(onkeyup: (String, Seq[String]) ⇒ IO[Unit]): IO[Unit] = io {
    allInputs foreach (input ⇒ {
      $(input).keyup((e: dom.Event) ⇒ {
        (for {
          _ ← OptionT(setInputWidth(input) map (_.some))
          methodName ← OptionT(io(methodParent(input)))
          exercise ← OptionT(io(findExerciseByMethod(methodName)))
          inputsValues = getInputsValues(exercise)
          _ ← OptionT(onkeyup(methodName, inputsValues) map (_.some))
        } yield ()).value.unsafePerformIO()
      })

  /** Set the class attribute to an exercise node
    */
  def setClass(e: HTMLElement, style: String): IO[Unit] = io {
    $(e).attr("class", s"exercise $style")
  }

  /** Write a message in the log of an exercise
    */
  def writeLog(e: HTMLElement, msg: String): IO[Unit] = io {
    $(e).find(".log").text(msg)
  }

  /** Assigns behaviors to the keyup event for inputs elements.
    */
  def onInputKeyUp(
    onkeyup:        (String, Seq[String]) ⇒ IO[Unit],
    onEnterPressed: String ⇒ IO[Unit]
  ): IO[Unit] = for {
    inputs ← allInputs
    _ ← inputs.map(input ⇒ attachKeyUpHandler(input, onkeyup, onEnterPressed)).sequence
  } yield ()

  def attachKeyUpHandler(
    input:          HTMLInputElement,
    onkeyup:        (String, Seq[String]) ⇒ IO[Unit],
    onEnterPressed: String ⇒ IO[Unit]
  ): IO[Unit] = io {
    $(input).keyup((e: dom.KeyboardEvent) ⇒ {
      (for {
        _ ← OptionT(setInputWidth(input) map (_.some))
        methodName ← OptionT(io(methodParent(input)))
        exercise ← OptionT(findExerciseByMethod(methodName))
        inputsValues = getInputsValues(exercise)
        _ ← {
          if (e.keyCode == KeyCode.enter)
            OptionT(onEnterPressed(methodName) map (_.some))
          else
            OptionT(onkeyup(methodName, inputsValues) map (_.some))
        }
      } yield ()).value.unsafePerformIO()
    })
  }

  /** Assigns behaviors to the blur event for inputs elements.
    */
  def onInputBlur(onBlur: String ⇒ IO[Unit]): IO[Unit] = for {
    inputs ← allInputs
    _ ← inputs.map(attachBlurHandler(_, onBlur)).sequence
  } yield ()

  def attachBlurHandler(input: HTMLInputElement, onBlur: String ⇒ IO[Unit]): IO[Unit] = io {
    $(input).blur((e: dom.Event) ⇒ {
      methodParent(input) foreach (onBlur(_).unsafePerformIO())
    })
  }

  def onButtonClick(onClick: String ⇒ IO[Unit]): IO[Unit] = for {
    exercises ← allExercises
    _ ← exercises.map(attachClickHandler(_, onClick)).sequence
  } yield ()

  def attachClickHandler(exercise: HTMLElement, onClick: String ⇒ IO[Unit]): IO[Unit] = io {
    $(exercise).find(".compile button").click((e: dom.Event) ⇒ {
      onClick(getMethodAttr(exercise)).unsafePerformIO()
    })
  }

  def setInputWidth(input: HTMLInputElement): IO[JQuery] =
    io($(input).width(inputSize(getInputLength(input))))

  def inputReplacements: IO[Seq[(HTMLElement, String)]] = for {
    blocks ← getCodeBlocks
  } yield blocks.map(code ⇒ code → replaceInputByRes(getTextInCode(code)))

  val resAssert = """(?s)\((res[0-9]*)\)""".r

  def allExercises: IO[List[HTMLDivElement]] = io {
    ($(".exercise").divs filter isMethodDefined).toList
  }

  def getMethodAttr(e: HTMLElement): String = $(e).attr("data-method").trim

  def isMethodDefined(e: HTMLElement): Boolean = getMethodAttr(e).nonEmpty

  def getLibrary: IO[String] = io { $("body").attr("data-library") }

  def getSection: IO[String] = io { $("body").attr("data-section") }

  def getMethodsList: IO[List[String]] = allExercises.map(_.map(getMethodAttr))

  def methodName(e: HTMLElement): Option[String] = Option(getMethodAttr(e)) filter (_.nonEmpty)

  def methodParent(input: HTMLInputElement): Option[String] = methodName($(input).closest(".exercise").getDiv)

  def allInputs: Seq[HTMLInputElement] = $(".exercise-code>input").inputs

  def findExerciseByMethod(method: String): IO[Option[HTMLElement]] = for {
    exercises ← allExercises
  } yield exercises.find(methodName(_) == Option(method))

  def getInputsValues(exercise: HTMLElement): Seq[String] = inputsInExercise(exercise).map(_.value)

  def inputsInExercise(exercise: HTMLElement): Seq[HTMLInputElement] = $(exercise).find("input").inputs

  def getCodeBlocks: Seq[HTMLElement] = $("pre code").elements

  def getTextInCode(code: HTMLElement): String = $(code).text

  def replaceInputByRes(text: String): String = resAssert.replaceAllIn(text, """(<input type="text" data-res="$1"/>)""")

  def getInputLength(input: HTMLInputElement): Int = $(input).value.toString.length

  def inputSize(length: Int): Double = length match {
    case 0 ⇒ 12d
    case _ ⇒ (12 + (length + 1) * 7).toDouble
  }

  implicit class JQueryOps(j: JQuery) {

    def elements: Seq[HTMLElement] = all[HTMLElement]

    def divs: Seq[HTMLDivElement] = all[HTMLDivElement]

    def inputs: Seq[HTMLInputElement] = all[HTMLInputElement]

    def getDiv: HTMLDivElement = get[HTMLDivElement]

    def all[A <: dom.Element]: Seq[A] = j.toArray().collect { case d: A ⇒ d }

    def get[A <: dom.Element]: A = j.get().asInstanceOf[A]

  }

}
