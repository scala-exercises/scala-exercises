package services.exerciseV0

import shared._

import scala.annotation.tailrec
import scalaz._, Scalaz._

/** Parses source code extracting libraries, sections, and exercise code.
  * This should provably be rewritten at some point with Parser Combinators or a more robust solution than
  * top to botton regex matching
  */
object ExerciseCodeExtractor {

  private[this] sealed trait ParserState

  private[this] case object DefiningClass extends ParserState

  private[this] case object AccCodeInDescription extends ParserState

  private[this] case object AccumulatingExercises extends ParserState

  private[this] case object AccCodeInExercise extends ParserState

  private[this] case object ExtractingCode extends ParserState

  private[this] case object AccCodeInExplanation extends ParserState

  private[this] val descLineSep = "<br>"

  private[this] val codeLineSep = "\n"

  private[this] val codeLineStart = "    "

  private[this] val codeBlockStart = """<pre><code class="scala">"""

  private[this] val codeBlockEnd = """</code></pre>"""

  private[this] case class SectionBuilder(
      name:            Option[String]   = None,
      description:     Option[String]   = None,
      currentExercise: Option[Exercise] = None,
      exercises:       List[Exercise]   = Nil,
      state:           ParserState      = DefiningClass
  ) {

    def categoryName(name: String) =
      copy(name = name.some)

    def addToSectionDescription(line: String) =
      copy(description = description.map(_.concat(descLineSep)) |+| line.some)

    def addCodeToSectionDescription(line: String) =
      copy(description = description.map(_.concat(codeLineSep)) |+| line.some)

    def setCurrentExercise(exercise: Exercise) =
      copy(currentExercise = exercise.some)

    def addCurrentExercise = currentExercise match {
      case Some(ex) ⇒ copy(exercises = exercises :+ ex, currentExercise = None)
      case None     ⇒ this
    }

    def setExerciseMethod(method: String) =
      copy(currentExercise = currentExercise map (e ⇒ e.copy(method = method.some)))

    def setExerciseName(name: String) =
      copy(currentExercise = currentExercise map (e ⇒ e.copy(name = name.some)))

    def addToExerciseDesc(line: String) =
      copy(currentExercise = currentExercise map (e ⇒ e.copy(description = e.description.map(_.concat(descLineSep)) |+| line.some)))

    def addCodeToExerciseDesc(line: String) =
      copy(currentExercise = currentExercise map (e ⇒ e.copy(description = e.description.map(_.concat(codeLineSep)) |+| line.some)))

    def addToExerciseCode(line: String) =
      copy(currentExercise = currentExercise map (e ⇒ e.copy(code = e.code.map(_.concat(codeLineSep)) |+| line.some)))

    def addToExerciseExplanation(line: String) =
      copy(currentExercise = currentExercise map (e ⇒ e.copy(explanation = e.explanation.map(_.concat(descLineSep)) |+| line.some)))

    def addCodeToExerciseExplanation(line: String) =
      copy(currentExercise = currentExercise map (e ⇒ e.copy(explanation = e.explanation.map(_.concat(codeLineSep)) |+| line.some)))

    def setState(state: ParserState) = copy(state = state)

    def build: Option[Section] = (name, description, exercises) match {
      case (Some(t), Some(d), head :: tail) ⇒ Some(Section(t, Some(d), head :: tail))
      case _                                ⇒ None
    }

  }

  private[this] val commentStart = "\\s*\\/\\*\\*(.*)".r
  private[this] val pkgStarts = "\\s*package\\s*object\\s+(\\w+).*".r
  private[this] val classStarts = "\\s*class\\s+(\\w+)\\s+extends.*exercise\\.Section.*".r
  private[this] val inComment = """(?s)\s*\*([^\n]+[^\/])$""".r
  private[this] val inCommentEmpty = """(?s)\s*\*(\s*)$""".r
  private[this] val codeInCommentStart = """(?s)\s*\*(\s*)\{\{\{$""".r
  private[this] val codeInCommentEnd = """(?s)\s*\*(\s*)\}\}\}$""".r
  private[this] val cologPkg = """(?s)\s*\*\s*Color:\s*(.*)$""".r
  private[this] val commentEnd = "\\s*\\*\\/([^\\/\\n]*).*".r
  private[this] val methodStart = "\\s*def\\s+(\\w+)(\\([^\\)]+\\))?(:\\s+ExerciseResult\\[.*\\])?\\s*=\\s*ExerciseRunner\\(\"(.+)\"\\)\\s*\\{".r
  private[this] val methodEnd = "\\s*\\}\\(∞\\).*".r

  import utils.StringUtils._

  /** Recursively walks the source lines building a Section and it's all contained exercises
    */
  def buildSection(sources: List[String]): Option[Section] = {
    @tailrec
    def loop(remaining: List[String], builder: SectionBuilder): SectionBuilder =
      remaining match {
        case Nil ⇒ builder
        case h :: t ⇒

          val modBuilder = (h, builder.state) match {
            case (commentStart(startLine), DefiningClass) ⇒
              builder.addToSectionDescription(startLine)
            case (codeInCommentStart(codeLine), DefiningClass) ⇒
              builder.addCodeToSectionDescription(codeBlockStart).setState(AccCodeInDescription)
            case (codeInCommentEnd(codeLine), AccCodeInDescription) ⇒
              builder.addToSectionDescription(codeBlockEnd).setState(DefiningClass)
            case (inComment(codeLine), AccCodeInDescription) ⇒
              builder.addCodeToSectionDescription(codeLine)
            case (inCommentEmpty(comment), AccCodeInDescription) ⇒
              builder.addToSectionDescription(codeLineSep)
            case (inComment(comment), DefiningClass) ⇒
              builder.addToSectionDescription(comment)
            case (classStarts(className), DefiningClass) ⇒
              builder.categoryName(className.humanizeCamelCase).setState(AccumulatingExercises)
            case (commentStart(comment), AccumulatingExercises) ⇒
              builder.setCurrentExercise(Exercise()).addToExerciseDesc(comment)
            case (codeInCommentStart(codeLine), AccumulatingExercises) ⇒
              builder.addCodeToExerciseDesc(codeBlockStart).setState(AccCodeInExercise)
            case (codeInCommentEnd(codeLine), AccCodeInExercise) ⇒
              builder.addToExerciseDesc(codeBlockEnd).setState(AccumulatingExercises)
            case (inComment(codeLine), AccCodeInExercise) ⇒
              builder.addCodeToExerciseDesc(codeLine)
            case (inCommentEmpty(comment), AccCodeInExercise) ⇒
              builder.addToExerciseDesc(codeLineSep)
            case (inComment(comment), AccumulatingExercises) ⇒
              builder.addToExerciseDesc(comment)
            case (methodStart(method, _, _, friendlyDes), AccumulatingExercises) ⇒
              builder.setExerciseName(friendlyDes).setExerciseMethod(method).setState(ExtractingCode)
            case (methodEnd(), ExtractingCode) ⇒
              builder.addCurrentExercise.setState(AccumulatingExercises)
            case (commentStart(comment), ExtractingCode) ⇒
              builder.addToExerciseExplanation(comment)
            case (codeInCommentStart(codeLine), ExtractingCode) ⇒
              builder.addCodeToExerciseExplanation(codeBlockStart).setState(AccCodeInExplanation)
            case (codeInCommentEnd(codeLine), AccCodeInExplanation) ⇒
              builder.addToExerciseExplanation(codeBlockEnd).setState(ExtractingCode)
            case (inComment(codeLine), AccCodeInExplanation) ⇒
              builder.addCodeToExerciseExplanation(codeLine)
            case (inCommentEmpty(comment), AccCodeInExplanation) ⇒
              builder.addToExerciseExplanation(codeLineSep)
            case (inComment(comment), ExtractingCode) ⇒
              builder.addToExerciseExplanation(comment)
            case (commentEnd(comment), ExtractingCode) ⇒
              builder.addToExerciseExplanation(comment)
            case (code, ExtractingCode) ⇒
              builder.addToExerciseCode(code)
            case _ ⇒ builder
          }
          loop(t, modBuilder)
      }
    loop(sources, SectionBuilder()).build
  }

  /** Recursive walks a package object sources parsing section information
    */
  def buildLibrary(sources: List[String]): Option[Library] = {
    @tailrec
    def loop(remaining: List[String], maybeLibrary: Option[Library] = None): Option[Library] =
      remaining match {
        case Nil ⇒ maybeLibrary
        case h :: t ⇒
          val modSection = (h, maybeLibrary) match {
            case (commentStart(startLine), None) ⇒
              Library(name = "", color = "", description = startLine).some
            case (cologPkg(color), Some(library)) ⇒
              library.copy(color = color).some
            case (inComment(comment), Some(library)) ⇒
              library.copy(description = library.description.concat(comment)).some
            case (pkgStarts(pkgName), Some(library)) ⇒
              library.copy(name = pkgName).some
            case (_, Some(library)) ⇒
              library.some
            case _ ⇒ None
          }
          loop(t, modSection)
      }
    loop(sources)
  }

}
