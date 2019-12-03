/*
 *  scala-exercises
 *
 *  Copyright 2015-2019 47 Degrees, LLC. <http://www.47deg.com>
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

import cats.effect.IO
import cats.implicits._
import org.scalaexercises.algebra.github.GithubOps
import org.scalaexercises.algebra.user.UserOps
import org.scalaexercises.exercises.Secure
import org.scalaexercises.exercises.utils.ConfigUtils
import org.scalaexercises.types.github.GithubUser
import org.scalaexercises.types.user.UserCreation
import play.api.mvc.{BaseController, ControllerComponents}
import play.api.{Configuration, Mode}

class OAuthController(conf: Configuration, components: ControllerComponents)(
    implicit userOps: UserOps[IO],
    githubOps: GithubOps[IO],
    mode: Mode)
    extends BaseController {

  private val configUtils = ConfigUtils(conf)

  def callback(codeOpt: Option[String] = None, stateOpt: Option[String] = None) =
    Secure(Action.async { implicit request ⇒
      (codeOpt, stateOpt, request.session.get("oauth-state")).tupled
        .fold(IO.pure(BadRequest("Missing `code` or `state`"))) {
          case (code, state, oauthState) ⇒
            if (state == oauthState) {
              githubOps
                .getAccessToken(
                  configUtils.githubAuthId,
                  configUtils.githubAuthSecret,
                  code,
                  configUtils.callbackUrl,
                  state)
                .map { ot =>
                  Redirect(configUtils.successUrl).withSession("oauth-token" → ot.accessToken)
                }
            } else IO.pure(BadRequest("Invalid github login"))
        }
        .unsafeToFuture()
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
    Secure(Action.async {
      implicit request ⇒
        request.session
          .get("oauth-token")
          .fold(IO.pure(Unauthorized("Missing OAuth token"))) { accessToken ⇒
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
          .unsafeToFuture()
    })

  def logout() = Secure(Action(Redirect("/").withNewSession))

  override protected def controllerComponents: ControllerComponents = components
}
