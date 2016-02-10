package com.fortysevendeg.exercises.utils

import com.fortysevendeg.exercises.persistence.domain.SaveUserProgress.{ ExerciseType, Other, Koans }

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

  implicit class CamelCaseString(s: String) {
    def humanizeCamelCase: String = StringUtils.camelCaseToHumanReadable(s)

    def removeSpaces: String = StringUtils.camelCaseWithoutSpaces(s)
  }

  object ExerciseType {
    def fromString(value: String): ExerciseType =
      Vector(Koans, Other).find(_.toString == value) getOrElse Other
  }

}
