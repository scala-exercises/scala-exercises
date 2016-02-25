/*
 * scala-exercises-content
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package shapeless
package exercises

/** shapeless
  *
  * Shapeless is a type class and dependent type based generic programming library for Scala.
  */
object ShapelessLib extends exercise.Library {
  override def color = Some("#6573C4")

  override def sections = List(
    PolyExercises,
    HListExercises
  )
}
