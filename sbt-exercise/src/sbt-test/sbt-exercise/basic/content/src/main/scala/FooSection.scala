package stdlib

import org.scalaexercises.definitions.Section

/** This is a Section Foo
  *
  * @param name foo
  */
object FooSection extends Section {

  /** Exercise foo 1 */
  def foo1(value: String): String = s"foo 1: $value"

  /** Exercise foo 2 */
  def foo2(value: String) {
    println(s"foo 2: $value")
    FooBarHelper.help()
  }

}
