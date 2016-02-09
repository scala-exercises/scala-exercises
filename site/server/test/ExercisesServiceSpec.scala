import com.fortysevendeg.exercises.models._
import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import com.fortysevendeg.exercises.services._
import shared._

@RunWith(classOf[JUnitRunner])
class ExercisesServiceSpec extends Specification {

  val expectedLibrary = "stdlib"
  val expectedTestSection = "Extractors"
  val expectedTestExercise = "forAssigningValues"
  val expectedTestSuccesArgs = List("Chevy", "Camaro", "1978", "120")
  val expectedTestFailedArgs = List("a", "b", "1", "2")

  "ExercisesService" should {

    "return at least one library via classpath discovery" in {
      val libraries = ExercisesService.libraries
      libraries must not be empty
      libraries.find(_.description == expectedLibrary) must beSome
    }.pendingUntilFixed("Have to fix test runs in Travis CI first")

    "return at least one category via classpath discovery" in {
      val foundSections = for {
        library ← ExercisesService.libraries
        section ← ExercisesService.section(library.description, expectedTestSection)
      } yield section
      foundSections must not be empty
      val expectedCat = foundSections.find(_.description == expectedTestSection)
      expectedCat must beSome
      val category = expectedCat.get
      category.exercises must not be empty
      category.exercises.find(_.method.contains(expectedTestExercise)) must beSome
    }.pendingUntilFixed("Have to fix test runs in Travis CI first")

    "evaluate a known exercise type coercing it's parameters and get a successful result" in {
      ExercisesService.evaluate(ExerciseEvaluation(
        libraryName = expectedLibrary,
        sectionName = expectedTestSection,
        method = expectedTestExercise,
        args = expectedTestSuccesArgs
      )).isRight must beTrue
    }.pendingUntilFixed("Have to fix test runs in Travis CI first")

    "evaluate a known exercise type coercing it's parameters and get a failed result" in {
      ExercisesService.evaluate(ExerciseEvaluation(
        libraryName = expectedLibrary,
        sectionName = expectedTestSection,
        method = expectedTestExercise,
        args = expectedTestFailedArgs
      )).isLeft must beTrue
    }

  }
}
