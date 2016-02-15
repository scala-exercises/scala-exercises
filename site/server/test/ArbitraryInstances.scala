import cats.data.Xor
import com.fortysevendeg.exercises.models.{UserDoobieStore, UserCreation}
import com.fortysevendeg.exercises.models.UserCreation.Request
import com.fortysevendeg.exercises.persistence.domain.SaveUserProgress
import doobie.imports._
import org.scalacheck.Arbitrary
import org.scalatest.Assertions
import shapeless.tag.@@
import shared.{User, UserProgress}
import org.scalacheck.Arbitrary._

import scalaz.concurrent.Task

trait ArbitraryInstances extends Assertions {

  type Id = Long

  implicit val userProgressArbitrary : Arbitrary[Id]= {
    Arbitrary(arbitrary[UserProgress].map(u => save(u).id))
  }



  implicit def persistentUserArbitrary(implicit transactor: Transactor[Task]): Arbitrary[User] = {
    import UserCreation._
    Arbitrary(arbitrary[Request] map { request =>
      UserDoobieStore.create(request).transact(transactor).run match {
        case Xor.Right(user) => user
        case Xor.Left(error) => fail("Failed generating persistent users : $error")
      }
    })
  }

  implicit def saveUserProgressArbitrary : Arbitrary[SaveUserProgress.Request] = {
    Arbitrary(for {
      user <- arbitrary[User]
      request <- arbitrary[SaveUserProgress.Request] map (p => p.copy(userId = user.id))
    } yield request)
  }
}
