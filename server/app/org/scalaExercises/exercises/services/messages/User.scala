/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaExercises.exercises.services.messages

import shared.User

case class GetUserOrCreateRequest(
  login:      String,
  name:       String,
  githubId:   String,
  pictureUrl: String,
  githubUrl:  String,
  email:      String
)

case class GetUserOrCreateResponse(user: User)

case class GetUserByLoginRequest(login: String)

case class GetUserByLoginResponse(user: Option[User] = None)

case class CreateUserRequest(
  login:      String,
  name:       String,
  githubId:   String,
  pictureUrl: String,
  githubUrl:  String,
  email:      String
)

case class CreateUserResponse(user: User)
