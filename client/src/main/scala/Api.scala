/*
 * scala-exercises-client
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.client
package api

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import model._
import config.Routes
import org.scalaexercises.types.exercises._
import org.scalaexercises.types.progress._

import messages.{ EvaluationRequest, EvaluationResult }

import upickle._
import common.ExtAjax._

import org.scalajs.dom.ext.{ Ajax, AjaxException }

object Client {
  def readProgress(library: String, section: String, raw: String): List[ClientExercise] = {
    val parsedBody = read[SectionExercises](raw)
    parsedBody.exercises.map(e ⇒ {
      ClientExercise(
        library = library,
        section = section,
        method = e.methodName,
        arguments = e.args,
        state = if (e.succeeded) Solved else Unsolved
      )
    })
  }

  def fetchProgress(library: String, section: String): Future[Option[List[ClientExercise]]] = {
    val url = Routes.Exercises.progress(library, section)
    Ajax.get(url).collect({
      case r if r.ok ⇒ Some(readProgress(library, section, r.responseText))
    }).recover({ case exc: AjaxException ⇒ None })
  }

  def compileExercise(e: ClientExercise): Future[EvaluationResult] = {
    val url = Routes.Exercises.evaluate(e.library, e.section)
    //TODO: TBD version and exercise types
    val request = EvaluationRequest(e.library, e.section, e.method, 1, "Koans", e.arguments)
    Ajax.postAsJson(url, write(request)).map(r ⇒ {
      if (r.ok)
        EvaluationResult(true, e.method)
      else
        EvaluationResult(false, e.method, r.responseText)
    }).recover({
      case exc: AjaxException ⇒ {
        EvaluationResult(
          false,
          e.method,
          if (exc.isTimeout)
            "We couldn't evaluate your exercise. You may be experiencing internet connectivity issues."
          else
            exc.xhr.responseText
        )
      }
    })
  }
}
