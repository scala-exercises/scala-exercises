/*
 *  scala-exercises
 *
 *  Copyright 2015-2017 47 Degrees, LLC. <http://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.scalaexercises.exercises.controllers

import cats.implicits._
import org.scalaexercises.exercises.Secure
import org.scalaexercises.algebra.app._
import org.scalaexercises.algebra.github.GithubOps
import org.scalaexercises.algebra.user.UserOps
import org.scalaexercises.types.user.UserCreation
import org.scalaexercises.types.github.GithubUser
import org.scalaexercises.exercises.services.interpreters.ProdInterpreters
import org.scalaexercises.exercises.utils.ConfigUtils._
import doobie.imports._
import play.api.Logger
import play.api.mvc.{Action, Controller, Result}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scalaz.concurrent.Task
import freestyle._
import freestyle.implicits._

class OAuthController(
    implicit userOps: UserOps[ExercisesApp.Op],
    githubOps: GithubOps[ExercisesApp.Op],
    T: Transactor[Task]
) extends Controller
    with ProdInterpreters {

  def callback(codeOpt: Option[String] = None, stateOpt: Option[String] = None) =
    Secure(Action.async {
      implicit request ⇒
        (codeOpt |@| stateOpt |@| request.session.get("oauth-state")).tupled
          .fold[FreeS[ExercisesApp.Op, Result]](
            FreeS.pure(BadRequest("Missing `code` or `state`"))) {
            case (code, state, oauthState) ⇒
              if (state == oauthState) {
                githubOps
                  .getAccessToken(githubAuthId, githubAuthSecret, code, callbackUrl, state)
                  .map { ot =>
                    Redirect(successUrl).withSession("oauth-token" → ot.accessToken)
                  }
              } else FreeS.pure(BadRequest("Invalid github login"))
          }
    })

  def createUserRequest(githubUser: GithubUser): UserCreation.Request =
    UserCreation.Request(
      login = githubUser.login,
      name = githubUser.name,
      githubId = githubUser.login,
      pictureUrl = githubUser.avatar,
      githubUrl = githubUser.url,
      email = githubUser.email
    )

  def success() =
    Secure(Action.async { implicit request ⇒
      request.session
        .get("oauth-token")
        .fold[FreeS[ExercisesApp.Op, Result]](FreeS.pure(Unauthorized("Missing OAuth token"))) {
          accessToken ⇒
            val ops = for {
              ghuser ← githubOps.getAuthUser(Some(accessToken))
              user   ← userOps.getOrCreate(createUserRequest(ghuser))
            } yield (ghuser, user)

            ops.map {
              case (ghu, u) ⇒
                Redirect(request.headers.get("referer") match {
                  case Some(url) if !url.contains("github") ⇒ url
                  case _                                    ⇒ "/"
                }).withSession("oauth-token" → accessToken, "user" → ghu.login)
            }
        }
    })

  def logout() = Secure(Action(Redirect("/").withNewSession))

}
