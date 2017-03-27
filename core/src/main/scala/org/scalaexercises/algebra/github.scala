/*
 * scala-exercises - core
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
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
