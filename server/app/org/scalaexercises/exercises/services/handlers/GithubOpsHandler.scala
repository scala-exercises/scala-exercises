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

package org.scalaexercises.exercises.services.handlers

import cats.effect.Sync
import cats.implicits._
import github4s.Github
import Github._
import github4s.jvm.Implicits._
import github4s.free.interpreters.{Capture => GithubCapture, Interpreters => GithubInterpreters}
import github4s.GithubResponses.{GHResponse, GHResult}
import org.scalaexercises.algebra.github.GithubOps
import org.scalaexercises.types.github.{Authorize, GithubUser, OAuthToken, Repository}
import scalaj.http.HttpResponse

class GithubOpsHandler[F[_]](implicit F: Sync[F]) extends GithubOps[F] {

  implicit val gitHubTaskCaptureInstance = new GithubCapture[F] {
    override def capture[A](a: => A): F[A] = F.delay(a)
  }

  implicit val githubInterpreter: GithubInterpreters[F, HttpResponse[String]] =
    new GithubInterpreters[F, HttpResponse[String]]

  override def getAuthorizeUrl(
      clientId: String,
      redirectUri: String,
      scopes: List[String]): F[Authorize] = {
    val ghResponse =
      Github().auth.authorizeUrl(clientId, redirectUri, scopes).exec[F, HttpResponse[String]]()
    ghResponseToEntity(ghResponse)(auth => Authorize(auth.url, auth.state))
  }

  override def getAccessToken(
      clientId: String,
      clientSecret: String,
      code: String,
      redirectUri: String,
      state: String): F[OAuthToken] = {
    val ghResponse = Github().auth
      .getAccessToken(clientId, clientSecret, code, redirectUri, state)
      .exec[F, HttpResponse[String]]()
    ghResponseToEntity(ghResponse)(token => OAuthToken(token.access_token))
  }

  override def getAuthUser(accessToken: Option[String]): F[GithubUser] = {
    val ghResponse = Github(accessToken).users.getAuth.exec[F, HttpResponse[String]]()
    ghResponseToEntity(ghResponse)(
      user =>
        GithubUser(
          login = user.login,
          name = user.name,
          avatar = user.avatar_url,
          url = user.html_url,
          email = user.email
      ))
  }

  override def getRepository(owner: String, repo: String): F[Repository] = {
    val ghResponse =
      Github(sys.env.get("GITHUB_TOKEN")).repos.get(owner, repo).exec[F, HttpResponse[String]]()
    ghResponseToEntity(ghResponse)(
      repo =>
        Repository(
          subscribers = repo.status.subscribers_count.getOrElse(0),
          stargazers = repo.status.stargazers_count,
          forks = repo.status.forks_count
      ))
  }

  private def ghResponseToEntity[A, B](response: F[GHResponse[A]])(f: A => B): F[B] =
    response.flatMap {
      case Right(GHResult(result, _, _)) => F.pure(f(result))
      case Left(e)                       => F.raiseError[B](e)
    }
}
