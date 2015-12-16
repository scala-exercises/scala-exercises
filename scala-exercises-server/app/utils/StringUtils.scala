package utils

object StringUtils {

  def camelCaseToHumanReadable(s: String): String = {
    s.replaceAll(
      String.format("%s|%s|%s",
        "(?<=[A-Z])(?=[A-Z][a-z])",
        "(?<=[^A-Z])(?=[A-Z])",
        "(?<=[A-Za-z])(?=[^A-Za-z])"
      ),
      " "
    )
  }

  implicit class CamelCaseString(s: String) {
    def humanizeCamelCase: String = StringUtils.camelCaseToHumanReadable(s)
  }

}
