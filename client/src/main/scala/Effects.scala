/*
 * Copyright 2014-2020 47 Degrees Open Source <https://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.scalaexercises.client
package effects

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import utils.DomHandler
import actions._
import model._
import model.Exercises._
import api.Client

object Effects {
  def noop: Future[Option[Action]] = Future(None)

  def perform(s: State, a: Action): Future[Option[Action]] =
    a match {
      case Start                   => loadInitialData
      case CompileExercise(method) => compileExercise(s, method)
      case _                       => noop
    }

  def loadInitialData: Future[Option[Action]] = {
    DomHandler.libraryAndSection.fold(Future(None): Future[Option[Action]]) { libAndSection =>
      val (lib, sect) = libAndSection
      Client
        .fetchProgress(lib, sect)
        .collect { case Some(state) =>
          Some(SetState(validateState(state)))
        }
    }
  }

  def compileExercise(s: State, method: String): Future[Option[Action]] =
    findByMethod(s, method) match {
      case Some(exercise) if exercise.isFilled =>
        Client
          .compileExercise(exercise)
          .map { result =>
            if (result.ok)
              Some(CompilationOk(result.method))
            else
              Some(CompilationFail(result.method, result.msg))
          }
      case _ => noop
    }

  def validateState(state: List[ClientExercise]): List[ClientExercise] =
    state.foldLeft(List.empty[ClientExercise]) {
      (acc: List[ClientExercise], exercise: ClientExercise) =>
        DomHandler
          .findExerciseByMethod(exercise.method)
          .map(DomHandler.inputsInExercise)
          .fold(acc) { inputs =>
            if (exercise.arguments.size == inputs.size)
              acc :+ exercise
            else
              acc :+ exercise.copy(arguments = Seq.empty, state = Unsolved)
          }
    }

}
