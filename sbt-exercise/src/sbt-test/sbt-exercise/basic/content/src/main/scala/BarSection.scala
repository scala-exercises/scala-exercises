package stdlib

import org.scalaexercises.definitions.Section

/** This is a Section
  *
  * @param name Section Bar
  */
object BarSection extends Section {

  /** Exercise bar 1 */
  def bar1(value: String) {
    println(s"bar 1: $value")
  }

  /** Exercise bar 2 */
  def bar2(value: String) {
    println(s"bar 2: $value")
    FooBarHelper.help()
  }

}
