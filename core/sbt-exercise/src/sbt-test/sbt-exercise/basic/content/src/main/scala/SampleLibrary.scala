package stdlib

/** This is my Library
  * @param name sample
  */
object SampleLibrary extends exercise.Library {

  override def sections = List(
    FooSection,
    BarSection
  )

}
