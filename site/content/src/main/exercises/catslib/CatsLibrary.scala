package catslib

/** cats
  *
  * These are exercises of the cats library.
  */
object CatsLibrary extends exercise.Library {
  override def color = Some("#4CAAF6")

  override def sections = List(
    IdentitySection,
    XorSection
  )
}
