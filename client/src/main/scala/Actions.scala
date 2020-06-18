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
package actions

import model.Exercises._

sealed trait Action
case object Start                                            extends Action
case class SetState(s: State)                                extends Action
case class UpdateExercise(method: String, args: Seq[String]) extends Action
case class CompileExercise(method: String)                   extends Action
case class CompilationOk(method: String)                     extends Action
case class CompilationFail(method: String, msg: String)      extends Action
