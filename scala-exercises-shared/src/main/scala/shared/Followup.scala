package shared


case class Followup(
    id: Option[Long],
    login: String,
    section: String,
    category: String,
    status: String)