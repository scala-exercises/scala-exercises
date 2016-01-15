import models.ExerciseEvaluation
import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import org.specs2.scalaz.DisjunctionMatchers
import services.parser.ExercisesService

@RunWith(classOf[JUnitRunner])
class ExercisesServiceSpec extends Specification with DisjunctionMatchers {

  val expectedTestSection = "stdlib"
  val expectedTestCategory = "Extractors"
  val expectedTestExercise = "forAssigningValues"
  val expectedTestSuccesArgs = List("Chevy", "Camaro", "1978", "120")
  val expectedTestFailedArgs = List("a", "b", "1", "2")

  "ExercisesService" should {

    "return at least one section via classpath discovery" in {
      val sections = ExercisesService.sections
      sections must not be empty
      sections.find(_.title == expectedTestSection) must beSome
    }

    "return at least one category via classpath discovery" in {
      val foundCategories = for {
        section ← ExercisesService.sections
        categoryS ← section.categories
        category ← ExercisesService.category(section.title, categoryS)
      } yield category
      foundCategories must not be empty
      val expectedCat = foundCategories.find(_.title == expectedTestCategory)
      expectedCat must beSome
      val category = expectedCat.get
      category.exercises must not be empty
      category.exercises.find(_.method.contains(expectedTestExercise)) must beSome
    }

    "evaluate a known exercise type coercing it's parameters and get a successful result" in {
      ExercisesService.evaluate(ExerciseEvaluation(
        section = expectedTestSection,
        category = expectedTestCategory,
        method = expectedTestExercise,
        args = expectedTestSuccesArgs)) must beRightDisjunction
    }

    "evaluate a known exercise type coercing it's parameters and get a failed result" in {
      ExercisesService.evaluate(ExerciseEvaluation(
        section = expectedTestSection,
        category = expectedTestCategory,
        method = expectedTestExercise,
        args = expectedTestFailedArgs)) must beLeftDisjunction
    }

  }
}