package org.scalaexercises.algebra.github

import cats.free._
import org.scalaexercises.types.github._
import io.freestyle._

/** Exposes GitHub operations as a Free monadic algebra that may be combined with other Algebras via
  * Coproduct
  */
@free trait GithubOps[F[_]] {

  def getAuthorizeUrl(
    clientId:    String,
    redirectUri: String,
    scopes:      List[String] = List.empty
  ): Free[F, Authorize]

  def getAccessToken(
    clientId:     String,
    clientSecret: String,
    code:         String,
    redirectUri:  String,
    state:        String
  ): Free[F, OAuthToken]

  def getAuthUser(accessToken: Option[String] = None): Free[F, GithubUser]

  def getRepository(owner: String, repo: String): Free[F, Repository]

}
