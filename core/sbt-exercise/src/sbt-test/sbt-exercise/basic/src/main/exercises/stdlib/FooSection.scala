package stdlib

/** Section Bar
  *
  * This is a Section
  */
object BarSection extends exercise.Section {

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
