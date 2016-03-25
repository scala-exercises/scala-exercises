package stdlib

/** This is my Library
  * @param name My Library
  */
object SampleLibrary extends exercise.Library {

  override def sections = List(
    FooSection,
    BarSection
  )

}
