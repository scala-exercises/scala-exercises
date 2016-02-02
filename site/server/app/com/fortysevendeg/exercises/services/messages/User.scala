package com.fortysevendeg.exercises.services.messages

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
