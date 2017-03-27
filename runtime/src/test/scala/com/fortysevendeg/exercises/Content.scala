/*
 * scala-exercises - runtime
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.content

import org.scalaexercises.runtime.model.Library

object LibraryA extends Library {
  override def owner         = ???
  override def repository    = ???
  override def color         = ???
  override def logoPath      = ???
  override def logoData      = ???
  override def description   = ???
  override def name          = ???
  override def sections      = ???
  override def timestamp     = ???
  override def buildMetaInfo = ???
}

object LibraryB extends Library {
  override def owner         = ???
  override def repository    = ???
  override def color         = ???
  override def logoPath      = ???
  override def logoData      = ???
  override def description   = ???
  override def name          = ???
  override def sections      = ???
  override def timestamp     = ???
  override def buildMetaInfo = ???
}

object LibraryC extends Library {
  override def owner         = ???
  override def repository    = ???
  override def color         = ???
  override def logoPath      = ???
  override def logoData      = ???
  override def description   = ???
  override def name          = ???
  override def sections      = ???
  override def timestamp     = ???
  override def buildMetaInfo = ???
}

class ErrorLibrary extends Library {
  override def owner         = ???
  override def repository    = ???
  override def color         = ???
  override def logoPath      = ???
  override def logoData      = ???
  override def description   = ???
  override def name          = ???
  override def sections      = ???
  override def timestamp     = ???
  override def buildMetaInfo = ???
}
