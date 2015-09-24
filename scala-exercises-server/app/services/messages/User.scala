package services.messages

import shared.User

case class GetUserOrCreateRequest(
    login: String,
    name: String,
    github_id: String,
    picture_url: String,
    github_url: String,
    email: String)

case class GetUserOrCreateResponse(user: User)

case class GetUserByLoginRequest(login: String)

case class GetUserByLoginResponse(user: Option[User] = None)

case class CreateUserRequest(
    login: String,
    name: String,
    github_id: String,
    picture_url: String,
    github_url: String,
    email: String)

case class CreateUserResponse(user: User)
