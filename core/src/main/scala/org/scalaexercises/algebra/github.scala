package org.scalaexercises.algebra.github

import cats.free._
import org.scalaexercises.types.github._

/** GitHub Ops GADT
  */
sealed trait GithubOp[A]

final case class GetAuthorizeUrl(
  clientId:    String,
  redirectUri: String,
  scopes:      List[String]
) extends GithubOp[Authorize]

final case class GetAccessToken(
  clientId:     String,
  clientSecret: String,
  code:         String,
  redirectUri:  String,
  state:        String
) extends GithubOp[OAuthToken]

final case class GetAuthUser(accessToken: Option[String] = None) extends GithubOp[GithubUser]

final case class GetRepository(owner: String, repo: String) extends GithubOp[Repository]

/** Exposes GitHub operations as a Free monadic algebra that may be combined with other Algebras via
  * Coproduct
  */
class GithubOps[F[_]](implicit I: Inject[GithubOp, F]) {

  def getAuthorizeUrl(
    clientId:    String,
    redirectUri: String,
    scopes:      List[String] = List.empty
  ): Free[F, Authorize] =
    Free.inject[GithubOp, F](GetAuthorizeUrl(clientId, redirectUri, scopes))

  def getAccessToken(
    clientId:      String,
    clienteSecret: String,
    code:          String,
    redirectUri:   String,
    state:         String
  ): Free[F, OAuthToken] =
    Free.inject[GithubOp, F](GetAccessToken(clientId, clienteSecret, code, redirectUri, state))

  def getAuthUser(accessToken: Option[String] = None): Free[F, GithubUser] = Free.inject[GithubOp, F](GetAuthUser(accessToken))

  def getRepository(owner: String, repo: String): Free[F, Repository] = Free.inject[GithubOp, F](GetRepository(owner, repo))

}

/** Default implicit based DI factory from which instances of the GuthubOps may be obtained
  */
object GithubOps {
  implicit def instance[F[_]](implicit I: Inject[GithubOp, F]): GithubOps[F] = new GithubOps[F]
}
