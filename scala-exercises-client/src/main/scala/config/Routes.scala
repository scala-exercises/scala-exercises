package config

object Routes {

  object Exercises {
    val base = "/exercises/sections"
    def evaluate(section: String, category: String) = base + s"/$section/$category/evaluate"
  }

}
