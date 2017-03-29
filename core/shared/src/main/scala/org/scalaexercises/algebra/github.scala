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

package org.scalaexercises.algebra.github

import cats.free._
import org.scalaexercises.types.github._
import freestyle._

/** Exposes GitHub operations as a Free monadic algebra that may be combined with other Algebras via
  * Coproduct
  */
@free
trait GithubOps[F[_]] {

  def getAuthorizeUrl(
    clientId:    String,
    redirectUri: String,
    scopes:      List[String] = List.empty
  ): FreeS[F, Authorize]

  def getAccessToken(
    clientId:     String,
    clientSecret: String,
    code:         String,
    redirectUri:  String,
    state:        String
  ): FreeS[F, OAuthToken]

  def getAuthUser(accessToken: Option[String] = None): FreeS[F, GithubUser]

  def getRepository(owner: String, repo: String): FreeS[F, Repository]

}
