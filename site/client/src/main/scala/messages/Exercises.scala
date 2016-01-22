package messages

case class EvaluationRequest(
  libraryName: String,
  sectionName: String,
  method:      String,
  args:        Seq[String]
)

// TODO: moar info
case class EvaluationResult(
  ok:     Boolean,
  method: String,
  msg:    String  = ""
)
