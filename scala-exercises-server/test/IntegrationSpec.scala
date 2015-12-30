import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

class IntegrationSpec extends Specification {

  "Home page" should {

    "should contain Scala Exercise string" in {
      running(TestServer(3333), HTMLUNIT) { browser =>
        browser.goTo("http://localhost:3333/")
        browser.pageSource must contain("Scala Exercises")
      }
    }

  }

}
