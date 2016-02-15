/*
 * scala-exercises-content
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package foolib

/** Section Foo
  *
  * This is a Section
  */
object FooSection extends exercise.Section {

  /** Exercise foo 1!!!
    *
    * {{{
    *   // this is a code block
    * }}}
    */
  def foo1(value: String) {
    println(s"foo 1: $value")
  }

  /** Exercise foo 2 */
  def foo2(value: String) {
    println(s"foo 2: $value")
    FooBarHelper.help()
  }

}
