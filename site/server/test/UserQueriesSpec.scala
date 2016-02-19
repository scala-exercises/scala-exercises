/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

import org.specs2.mutable.Specification
import test.database.DatabaseInstance
import scalaz.concurrent.Task
import doobie.contrib.specs2.analysisspec.AnalysisSpec
import com.fortysevendeg.exercises.models.queries.Queries
import shared.User
import com.fortysevendeg.exercises.models.UserCreation

class UserQueriesSpec extends Specification with AnalysisSpec with DatabaseInstance {

  check(Queries.all)
  check(Queries.byLogin("47deg"))
  check(Queries.byId(47))
  check(Queries.insert(
    UserCreation.Request(
      login = "47deg",
      name = "47",
      githubId = "47deg",
      pictureUrl = "http://placekitten.com/50/50",
      githubUrl = "http://github.com/47deg",
      email = Some("hello@47deg.com")
    )
  ))
  check(Queries.deleteById(47))
  check(Queries.update(
    User(
      id = 47,
      login = "47deg",
      name = "47",
      githubId = "47deg",
      pictureUrl = "http://placekitten.com/50/50",
      githubUrl = "http://github.com/47deg",
      email = Some("hello@47deg.com")
    )
  ))
}
