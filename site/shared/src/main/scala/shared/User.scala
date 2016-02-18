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

case class UserProgress(
  id:           Long,
  userId:       Long,
  libraryName:  String,
  sectionName:  String,
  method:       String,
  version:      Int,
  exerciseType: String         = "Other",
  args:         Option[String],
  succeeded:    Boolean
)

case class OverallUserProgressItem(
  libraryName: String,
  sections:    Long,
  completed:   Boolean
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
)