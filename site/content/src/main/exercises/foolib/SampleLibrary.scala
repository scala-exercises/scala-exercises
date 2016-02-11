package foolib

/** My Library
  * This is my Library
  */
object SampleLibrary extends exercise.Library {
  override def sections = List(
    FooSection,
    BarSection
  )
}
