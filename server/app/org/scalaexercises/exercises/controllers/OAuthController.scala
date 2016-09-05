/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.exercises.controllers

import cats.data.Xor
import cats.std.option._
import cats.syntax.cartesian._

import org.scalaexercises.exercises.Secure

import org.scalaexercises.algebra.app._
import org.scalaexercises.algebra.github.GithubOps
import org.scalaexercises.algebra.user.UserOps

import org.scalaexercises.types.user.UserCreation
import org.scalaexercises.types.github.GithubUser

import org.scalaexercises.exercises.services.interpreters.FreeExtensions._
import org.scalaexercises.exercises.services.interpreters.ProdInterpreters
import org.scalaexercises.exercises.utils.OAuth2._

import doobie.imports._

import play.api.Logger
import play.api.mvc.{ Action, Controller }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scalaz.concurrent.Task

class OAuthController(
    implicit
    userOps:   UserOps[ExercisesApp],
    githubOps: GithubOps[ExercisesApp],
    T:         Transactor[Task]
) extends Controller with ProdInterpreters {

  def callback(codeOpt: Option[String] = None, stateOpt: Option[String] = None) = Secure(Action.async { implicit request ⇒

    (codeOpt |@| stateOpt |@| request.session.get("oauth-state")).tupled
      .fold(Future.successful(BadRequest("Missing `code` or `state`"))) {
        case (code, state, oauthState) ⇒
          if (state == oauthState) {
            githubOps.getAccessToken(githubAuthId, githubAuthSecret, code, callbackUrl, state).runFuture.map {
              case Xor.Right(a) ⇒ Redirect(successUrl).withSession("oauth-token" → a.accessToken)
              case Xor.Left(ex) ⇒ Unauthorized(ex.getMessage)
            }
          } else Future.successful(BadRequest("Invalid github login"))
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

  def success() = Secure(Action.async { implicit request ⇒
    request.session.get("oauth-token").fold(Future.successful(Unauthorized("Missing OAuth token"))) { accessToken ⇒
      val ops = for {
        ghuser ← githubOps.getAuthUser(Some(accessToken))
        user ← userOps.getOrCreate(createUserRequest(ghuser))
      } yield (ghuser, user)

      ops.runFuture.map {
        case Xor.Right((ghu, u)) ⇒ Redirect(request.headers.get("referer") match {
          case Some(url) if !url.contains("github") ⇒ url
          case _                                    ⇒ "/"
        }).withSession("oauth-token" → accessToken, "user" → ghu.login)
        case Xor.Left(error) ⇒ {
          Logger.error("Failed to save GitHub user information", error)
          InternalServerError("Failed to save user information")
        }
      }
    }

  })

  def logout() = Secure(Action(Redirect("/").withNewSession))

}
