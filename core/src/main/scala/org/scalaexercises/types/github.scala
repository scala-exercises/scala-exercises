/*
 * scala-exercises - core
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.types.github

case class GithubUser(
  login:  String,
  name:   Option[String],
  avatar: String,
  url:    String,
  email:  Option[String]
)

case class Repository(
  subscribers: Int,
  stargazers:  Int,
  forks:       Int
)

case class OAuthToken(
  accessToken: String
)

case class Authorize(
  url:   String,
  state: String
)
