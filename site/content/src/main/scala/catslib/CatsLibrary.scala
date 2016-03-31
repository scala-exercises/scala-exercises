package catslib

/** Cats is an experimental library intended to provide abstractions for functional programming in Scala.
  *
  * @param name cats
  */
object CatsLibrary extends exercise.Library {
  override def color = Some("#4CAAF6")

  override def sections = List(
    MonoidSection,
    SemigroupSection,
    FunctorSection,
    ApplySection,
    ApplicativeSection,
    MonadSection,
    FoldableSection,
    TraverseSection,
    IdentitySection,
    XorSection,
    ValidatedSection
  )
}
