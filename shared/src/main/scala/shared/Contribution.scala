package shared

case class Contribution(
  sha:       String,
  message:   String,
  timestamp: String,
  url:       String,
  author:    String,
  authorUrl: String,
  avatarUrl: String
)

case class Contributor(
  author:    String,
  authorUrl: String,
  avatarUrl: String
)