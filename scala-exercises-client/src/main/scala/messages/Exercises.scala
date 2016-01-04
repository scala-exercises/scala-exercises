package messages

case class EvaluationRequest(
  section: String,
  category: String,
  method: String,
  args: Seq[String])