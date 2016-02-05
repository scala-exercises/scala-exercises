package stdlib

/** Section Bar */
object SampleLibrary extends exercise.Library {

  override def sections = List(
    FooSection,
    BarSection
  )

}
