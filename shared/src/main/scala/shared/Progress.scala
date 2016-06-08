package shared

// Overall progress

case class OverallUserProgressItem(
    libraryName: String,
    completedSections: Int,
    totalSections: Int
) {
  def completed: Boolean = completedSections == totalSections
}

case class OverallUserProgress(libraries: List[OverallUserProgressItem])

// Per-exercise progress

case class ExerciseProgress(methodName: String, args: List[String], succeeded: Boolean)

case class SectionExercises(
    libraryName: String,
    sectionName: String,
    exercises: List[ExerciseProgress],
    totalExercises: Int
) {
  def sectionSucceeded = exercises.filter(_.succeeded).size == totalExercises
}

// Per-section and library progress

case class SectionProgress(
  sectionName: String,
  succeeded: Boolean
)

case class LibraryProgress(
    libraryName: String,
    sections: List[SectionProgress]
) {
  def totalSections: Int = sections.size
  def completedSections: Int = sections.filter(_.succeeded).size
}
