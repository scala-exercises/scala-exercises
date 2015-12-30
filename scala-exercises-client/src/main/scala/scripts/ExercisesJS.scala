package scripts

import scala.scalajs.js
import org.scalajs.dom
import org.scalajs.jquery.{ jQuery ⇒ $ }

object ExercisesJS extends js.JSApp {

  def main(): Unit = {
    runHighlight
  }

  def runHighlight = $("pre code").each({ (f: Any, code: dom.Element) ⇒
    js.Dynamic.global.hljs.highlightBlock(code)
  })

}
