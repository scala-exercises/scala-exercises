/*
 * scala-exercises-content
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package foolib

/** My Library
  *
  * This is my Library
  */
object SampleLibrary extends exercise.Library {
  override def sections = List(
    FooSection,
    BarSection
  )
}
