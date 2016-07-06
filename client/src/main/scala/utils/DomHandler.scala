/*
 * scala-exercises-client
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.client
package utils

import org.scalajs.dom.ext.KeyCode
import scala.scalajs.js
import org.scalajs.dom
import org.scalajs.dom.raw.{ HTMLDivElement, HTMLElement, HTMLInputElement }
import org.scalajs.jquery.{ jQuery ⇒ $, JQuery }
import fp.IO

import cats.data.OptionT
import cats.std.list._
import cats.std.option._
import cats.syntax.option._
import cats.syntax.traverse._

object DomHandler {

  import IO._

  /** Replaces text matched into html inputs
    */
  def replaceInputs(nodes: Seq[(HTMLElement, String)]): IO[Unit] = io {
    nodes foreach { case (n, r) ⇒ $(n).html(r) }
  }

  /** Highlights every preformatted code block.
    */
  def highlightCodeBlocks: IO[Unit] = io {
    $("pre").each((_: Any, code: dom.Element) ⇒ {
      js.Dynamic.global.hljs.highlightBlock(code)
    })
  }

  /** Converts emoji markup into inline emoji images.
    */
  def emojify: IO[Unit] = io {
    $(".modal-body").each((_: Any, el: dom.Element) ⇒ {
      js.Dynamic.global.emojify.run(el)
    })
  }

  /** Set the class attribute to an exercise node
    */
  def setExerciseClass(e: HTMLElement, style: String): IO[Unit] = io {
    $(e).attr("class", s"exercise $style")
  }

  /** Set the class attribute to an exercise code node
    */
  def setCodeClass(e: HTMLElement, style: String): IO[Unit] = io {
    $(e).attr("class", s"exercise-pre $style")
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

  /** Shows modal for signing up
    */
  def showSignUpModal: IO[Unit] = io($("#mustSignUp").modal("show"))

  def attachKeyUpHandler(
    input:          HTMLInputElement,
    onkeyup:        (String, Seq[String]) ⇒ IO[Unit],
    onEnterPressed: String ⇒ IO[Unit]
  ): IO[Unit] = io {
    $(input).keyup((e: dom.KeyboardEvent) ⇒ {
      (for {
        _ ← OptionT(setInputWidth(input) map (_.some))
        methodName ← OptionT(io(methodParent(input)))
        exercise ← OptionT(io(findExerciseByMethod(methodName)))
        inputsValues = getInputsValues(exercise)
        _ ← OptionT((e.keyCode match {
          case KeyCode.Enter ⇒ onEnterPressed(methodName)
          case _             ⇒ onkeyup(methodName, inputsValues)
        }).map(_.some))
      } yield ()).value.unsafePerformIO()
    })
  }

  def onButtonClick(onClick: String ⇒ IO[Unit]): IO[Unit] =
    allExercises.map(attachClickHandler(_, onClick)).sequence.map(_ ⇒ ())

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

  val resAssert = """(?s)(res\d+)""".r

  def allExercises: List[HTMLDivElement] = {
    ($(".exercise").divs filter isMethodDefined).toList
  }

  def getMethodAttr(e: HTMLElement): String = $(e).attr("data-method").getOrElse("").trim

  def isMethodDefined(e: HTMLElement): Boolean = getMethodAttr(e).nonEmpty

  def library: Option[String] = $("body").attr("data-library").toOption

  def section: Option[String] = $("body").attr("data-section").toOption

  def libraryAndSection: Option[(String, String)] = for {
    lib ← library
    sec ← section
  } yield (lib, sec)

  def methods: List[String] = allExercises.map(getMethodAttr(_))

  def methodName(e: HTMLElement): Option[String] = Option(getMethodAttr(e)) filter (_.nonEmpty)

  def methodParent(input: HTMLInputElement): Option[String] = methodName($(input).closest(".exercise").getDiv)

  def allInputs: IO[List[HTMLInputElement]] = io { $(".exercise-code>input").inputs.toList }

  def inputs(el: HTMLElement): List[HTMLInputElement] = $(el).find("input").inputs.toList

  def findExerciseByMethod(method: String): Option[HTMLElement] = {
    allExercises.find(methodName(_) == Option(method))
  }

  def findExerciseCode(el: HTMLElement): Option[HTMLElement] = {
    $(el).find(".exercise-pre").all.headOption
  }

  def getInputsValues(exercise: HTMLElement): Seq[String] = inputsInExercise(exercise).map(_.value)

  def inputsInExercise(exercise: HTMLElement): Seq[HTMLInputElement] = $(exercise).find("input").inputs

  def getCodeBlocks: IO[Seq[HTMLElement]] = io { $("code.exercise-code").elements }

  def getTextInCode(code: HTMLElement): String = $(code).text

  def replaceInputByRes(text: String): String = resAssert.replaceAllIn(text, """<input type="text" data-res="$1"/>""")

  def getInputLength(input: HTMLInputElement): Int = $(input).value.toString.length

  def isLogged: Boolean = $("#loggedUser").length > 0

  def setInputValue(input: HTMLInputElement, v: String): IO[Unit] = for {
    _ ← io { $(input) `val` (v) }
    _ ← setInputWidth(input)
  } yield ()

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

  @js.native
  trait BootstrapModal extends JQuery {
    def modal(action: String): BootstrapModal = js.native
  }

  implicit def jQueryToModal(jq: JQuery): BootstrapModal = jq.asInstanceOf[BootstrapModal]

}
