import org.scalaexercises.runtime._

import java.nio.file.Paths

object Check extends App {

  // check that the runtime layer works, including
  // an evaluation of FooSection.foo1

  val (errors, libraries) =
    Exercises.discoverLibraries(cl = Check.getClass.getClassLoader)

  assert(errors.isEmpty, "expected errors to be empty")
  assert(libraries.length == 1, "expected one library from the content project")


  val res = libraries.find(_.name == "sample")
    .flatMap(_.sections.find(_.name == "foo"))
    .flatMap(_.exercises.find(_.name == "foo1"))

  assert(res.isDefined, "evaluation of exercise method failed")
}