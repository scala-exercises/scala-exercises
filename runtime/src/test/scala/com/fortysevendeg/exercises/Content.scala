package defaultLib

import com.fortysevendeg.exercises.Library

object LibraryA extends Library {
  override def owner = ???
  override def repository = ???
  override def color = ???
  override def description = ???
  override def name = ???
  override def sections = ???
}

object LibraryB extends Library {
  override def owner = ???
  override def repository = ???
  override def color = ???
  override def description = ???
  override def name = ???
  override def sections = ???
}

object LibraryC extends Library {
  override def owner = ???
  override def repository = ???
  override def color = ???
  override def description = ???
  override def name = ???
  override def sections = ???
}

class ErrorLibrary extends Library {
  override def owner = ???
  override def repository = ???
  override def color = ???
  override def description = ???
  override def name = ???
  override def sections = ???
}
