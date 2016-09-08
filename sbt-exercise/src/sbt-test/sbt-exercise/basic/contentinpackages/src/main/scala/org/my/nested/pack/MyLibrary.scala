package org.my.nested.pack

import org.scalaexercises.definitions.Library

/** This is my Library in package org.my.nested.pack
  * @param name sample
  */
object MyLibrary extends Library {

  override def owner = "scala-exercises"
  override def repository = "scala-exercises"

  override def color = Option("#476047")

  override def sections = List(
    FooSection,
    BarSection
  )

  override def logoPath = "logo-path"

}
