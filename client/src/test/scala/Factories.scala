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
package factories

import model._

object Factories {
  def clientExercise(
      library: String = "",
      section: String = "",
      method: String = "",
      args: Seq[String] = Seq(),
      state: ExerciseState = Unsolved
  ): ClientExercise =
    ClientExercise(
      library = library,
      section = section,
      method = method,
      arguments = args,
      state = state
    )
}
