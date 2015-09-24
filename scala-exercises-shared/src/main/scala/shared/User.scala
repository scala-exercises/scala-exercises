package shared

case class User(
    id: Option[Long],
    login: String,
    name: String,
    github_id: String,
    picture_url: String,
    github_url: String,
    email: String)