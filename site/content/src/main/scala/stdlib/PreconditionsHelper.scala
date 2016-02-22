package stdlib

object PreconditionsHelper {

  class WithParameterRequirement(val myValue: Int) {
    require(myValue != 0)

    def this(someValue: String) {
      this(someValue.size)
    }
  }

}
