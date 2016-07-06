package org.scalaexercises.content

import org.scalaexercises.runtime.model.Library

object LibraryA extends Library {
  override def owner = ???
  override def repository = ???
  override def color = ???
  override def description = ???
  override def name = ???
  override def sections = ???
  override def timestamp = ???
}

object LibraryB extends Library {
  override def owner = ???
  override def repository = ???
  override def color = ???
  override def description = ???
  override def name = ???
  override def sections = ???
  override def timestamp = ???
}

object LibraryC extends Library {
  override def owner = ???
  override def repository = ???
  override def color = ???
  override def description = ???
  override def name = ???
  override def sections = ???
  override def timestamp = ???
}

class ErrorLibrary extends Library {
  override def owner = ???
  override def repository = ???
  override def color = ???
  override def description = ???
  override def name = ???
  override def sections = ???
  override def timestamp = ???
}
