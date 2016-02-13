package exercise

/** Marker trait for exercise libraries.
  */
trait Library {
  def sections: List[Section]
  def color: Option[String] = None
}
