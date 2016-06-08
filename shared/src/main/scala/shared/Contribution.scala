package shared

case class Contributions(
  commits: List[Contribution],
  contributors: List[Contributor]
)

case class Contribution(
  sha: String,
  message: String,
  date: String,
  url: String,
  login: String,
  avatarUrl: String,
  authorUrl: String
)

case class Contributor(
  login: String,
  avatarUrl: String,
  authorUrl: String
)