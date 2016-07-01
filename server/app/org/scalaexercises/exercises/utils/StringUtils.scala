/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
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
