package shared

/** A category. For example `Extractors`
  */
case class Category(
    title: String,
    description: Option[String] = None,
    exercises: List[Exercise] = Nil)