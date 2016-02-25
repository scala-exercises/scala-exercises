package shared

case class User(
  id:         Long,
  login:      String,
  name:       Option[String],
  githubId:   String,
  pictureUrl: String,
  githubUrl:  String,
  email:      Option[String]
)

case class UserProgress(
  id:           Long,
  userId:       Long,
  libraryName:  String,
  sectionName:  String,
  method:       String,
  version:      Int,
  exerciseType: String       = "Other",
  args:         List[String],
  succeeded:    Boolean
)

case class OverallUserProgressItem(
  libraryName:   String,
  sections:      Int,
  totalSections: Int,
  completed:     Boolean
)

case class OverallUserProgress(libraries: List[OverallUserProgressItem])

case class LibrarySectionExercise(methodName: String, args: List[String], succeeded: Boolean)

case class LibrarySectionArgs(
  libraryName:      String,
  totalSections:    Int,
  exercises:        List[LibrarySectionExercise],
  librarySucceeded: Boolean
)

case class SectionInfoItem(sectionName: String, succeeded: Boolean)

case class LibrarySections(
    libraryName: String,
    sections:    List[SectionInfoItem]
) {
  def totalSections: Int = sections.size
  def completedSections: Int = sections.filter(_.succeeded).size
}
