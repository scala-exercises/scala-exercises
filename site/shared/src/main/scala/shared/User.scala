package shared

// TODO: is any of this actually used/needed by the client js code?

case class User(
  id:         Long,
  login:      String,
  name:       String,
  githubId:   String,
  pictureUrl: String,
  githubUrl:  String,
  email:      String
)
