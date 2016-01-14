package shared

// TODO: is any of this actually used/needed by the client js code?

case class User(
  id:          Option[Long],
  login:       String,
  name:        String,
  github_id:   String,
  picture_url: String,
  github_url:  String,
  email:       String
)
