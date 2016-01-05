package shared

/** A section representing a lib or lang. Ej. stdlib, cats, scalaz...
  */
case class Section(
    title: String,
    description: String,
    color: String,
    categories: List[String] = Nil)