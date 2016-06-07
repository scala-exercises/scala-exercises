package shared

case class User(
  id: Long,
  login: String,
  name: Option[String],
  githubId: String,
  pictureUrl: String,
  githubUrl: String,
  email: Option[String]
)

