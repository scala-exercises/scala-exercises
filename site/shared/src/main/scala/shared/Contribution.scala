package shared

case class Contributions(
  commits:      List[Contribution],
  contributors: List[Contributor]
)

case class Contribution(
  sha:        String,
  message:    String,
  date:       String,
  url:        String,
  login:      String,
  avatar_url: String,
  author_url: String
)

case class Contributor(
  login:      String,
  avatar_url: String,
  author_url: String
)
