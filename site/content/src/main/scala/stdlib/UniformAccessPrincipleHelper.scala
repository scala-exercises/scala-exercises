package stdlib

object UniformAccessPrincipleHelper {

  class CalculatesAgeUsingMethod(var currentYear: Int, birthYear: Int) {

    def age = currentYear - birthYear

    // calculated when method is called
  }

  class CalculatesAgeUsingProperty(var currentYear: Int, birthYear: Int) {
    // does age stay up to date if defined as a var instead of a val?
    val age = currentYear - birthYear
    // calculated at instantiation, returns property when called
  }

}
