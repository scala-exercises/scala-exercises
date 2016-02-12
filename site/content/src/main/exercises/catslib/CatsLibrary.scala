package catslib

/** Cats
  * These are exercises of the cats library.
  */
object CatsLibrary extends exercise.Library {
  override def sections = List(
    IdentitySection,
    XorSection
  )
}
