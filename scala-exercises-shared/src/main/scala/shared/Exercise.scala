package shared

/** Exercises within a Category
  */
case class Exercise(
    method: Option[String] = None,
    title: Option[String] = None,
    description: Option[String] = None,
    code: Option[String] = None,
    explanation: Option[String] = None)