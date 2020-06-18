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

package org.scalaexercises.exercises.utils

object StringUtils {

  def camelCaseToHumanReadable(s: String): String = {
    s.replaceAll(
      String.format(
        "%s|%s|%s",
        "(?<=[A-Z])(?=[A-Z][a-z])",
        "(?<=[^A-Z])(?=[A-Z])",
        "(?<=[A-Za-z])(?=[^A-Za-z])"
      ),
      " "
    )
  }

  def camelCaseWithoutSpaces(s: String): String = s.replaceAll("""\s""", "")

  def lowerUnderscoreToHumanReadable(s: String): String =
    s.replaceAll("_", " ").split(" ").map(_.capitalize).mkString(" ")

  implicit class CamelCaseString(s: String) {
    def humanizeCamelCase: String = StringUtils.camelCaseToHumanReadable(s)

    def removeSpaces: String = StringUtils.camelCaseWithoutSpaces(s)

    def humanizeUnderscore: String = StringUtils.lowerUnderscoreToHumanReadable(s)

  }

}
