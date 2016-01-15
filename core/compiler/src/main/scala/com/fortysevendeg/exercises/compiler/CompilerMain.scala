package com.fortysevendeg.exercises
package compiler

object CompilerMain extends App {
  println("Scala Exercise Compiler")
  println("Scala version " + scala.util.Properties.versionMsg)
  println("Exercise files: ")
  args.foreach { exercise ⇒
    println(s"> $exercise")
  }
}
