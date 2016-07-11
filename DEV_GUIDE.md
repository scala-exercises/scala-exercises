#Developer guide

##Contributing new content

In addition to including features / fixes, you can contribute new content to Scala Exercises! The platform is designed in a way that creating new exercises is pretty straight-forward. Currently there are several libraries available in production (i.e.: STD lib, Cats, Shapeless...), and more are to be included soon. If you're interested in creating a new exercises library, you'll find all the needed steps in this document.

###Overview

The `scala-exercises` organization holds different repositories for both the core project ("scala-exercises"), and each of the main exercises libraries (i.e.: "exercises-stdlib", "exercises-cats"...). The core project is currently built on one single module. The content you can provide to the platform doesn't necessarily be in the previously mentioned organization, but for them to appear inside the main `scala-exercises` in production, they need to be published in a public artifact repository like Sonatype or Maven-central. All the steps, from creating the content library to the publication process, are described in the following sections.

###Content creation

Scala Exercises content is organized in the following way:

```
Scala Exercises Core
       |
       | imports
       |
   Libraries
       |
       | contains
       |
    Sections
       |
       | contains
       |
    Exercises
```

####Creating a new content library

Each content library is an independent SBT module working as a library, which should be published as an artifact for it to be included in Scala Exercises. From now on, we'll consider you've already set up a new project for your exercises library, and we'll focus on the specifics on how to layout the content itself.

First of all you need to set up the library by creating a new Scala `object` that contains basic details about it. This `object` must subclass from `org.scalaexercises.definitions.Library`. Here you can find a template for this object:

```scala
package package_name (1)

import org.scalaexercises.definitions._

/** This line should describe your library shortly, and will appear as its description in the main Scala Exercises website. (2)
  *
  * @param name library_name (3)
  */
object MyLibrary extends Library {
  override def owner = "owner" (4)
  override def repository = "github_repository" (5)

  override def color = Some("#HEX_COLOR") (6)

  (7)
  override def sections = List(
    SectionA,
    SectionB,
    ...
    SectionN
  )
}
```

Let's dig into each one of these points:

* (1) A package name for your exercises library.
* (2) Inside the first [ScalaDoc](https://wiki.scala-lang.org/display/SW/Syntax) block, we define the description of the library as it'll appear in the list of the available libraries.
* (3) Also, this [ScalaDoc](https://wiki.scala-lang.org/display/SW/Syntax) block defines the title of the library. It'll also be used as the path suffix for its URL. If you need to use spaces, use an underscore symbol (i.e.: "STD_lib")
* (4) Owner username or organization in GitHub.
* (5) Name of the repository that will contain the library.
* (6) Color (in hexadecimal code format) that will identify the library in the website.
* (7) A list that contains every section you want to include in this library. Every item defined here is a Scala object containing the exercises per section.

####Creating a new section

As previously mentioned, exercises are organized in sections that will be based on a Scala `object`. A good practice would be to have each section in a separate Scala file. So basically, the basic flow to create new exercises would be:

1. Define the section `object` (we'll go through this process in a moment).
2. Include a reference to this object in the `sections` list in the library `object` described in the previous section.

How does a section look like?

```scala
import org.scalatest._
import org.scalaexercises.definitions.Section

/** @param name section_title (1)
*/
object MySection extends FlatSpec with Matchers with Section (2) {
    // exercises definition (3)
}
```

* (1) As when defining the library, you need to use [ScalaDoc](https://wiki.scala-lang.org/display/SW/Syntax) to define the name of the section. Use an underscore symbol to include spaces.
* (2) Each section `object` should inherit from `org.scalaexercises.definitions.Section`. `FlatSpec` and `Matchers` are traits from the ScalaTest library, the framework Scala Exercises uses to check the results.
* (3) Inside the section `object` we can start defining the exercises, as it'll be shown in the following section.


####Creating new exercises

The content for each exercise is defined in a [ScalaDoc](https://wiki.scala-lang.org/display/SW/Syntax) block, with some specific particularities.

```scala
/** = Title = (1)
*
* Text describing background about the exercise, can be as long as needed. (2)
*
* {{{
*   // code block (3)
* }}}
*
* Documentation can be broken in as many paragraphs as needed.
*/
def functionAssert(res0: Boolean): Unit {
    true shouldBe res0  // (4)
}
```

* (1) You can define titles in the documentation for your library wrapping it inside a pair of equal signs (=), defining a [ScalaDoc](https://wiki.scala-lang.org/display/SW/Syntax) heading.
* (2) The documentation itself can be splitted up in several paragraphs, depending on your needs. You can use backquotes (`) to highlight an specific item or a code reference.
* (3) Code that illustrates your exercises should be surrounded by triple curly braces.
* (4) Code that defines your exercises is contained in a Scala function below the [ScalaDoc](https://wiki.scala-lang.org/display/SW/Syntax) containing the documentation.

Exercise functions (as described in (4)) should receive one parameter per placeholder that users will fill in the UI, and return `Unit`. You can include as many placeholders as needed, with a minimum of one. Inside the function, you can include nested functions that your exercise may need to work, and a set of ScalaTest asserts checking the user's inputs.

Currently Scala Exercises only supports this kind of input (i.e.: filling placeholders). Potentially it'll allow writing complete code snippets from the users in order to complete an exercise.

####Local development

In order to see and debug your exercises in your local environment, you need to publish them locally and include them as a dependency for your local instance. Here are the steps to achieve this:

* (1) Compile your exercises (i.e.: `sbt compile`). This is needed for each change, as the publication process can fail otherwise.
* (2) Publish your excersises locally (i.e.: `sbt publishLocal`).
* (3) Run [Scala Exercises](https://github.com/scala-exercises/scala-exercises) in your local development environment (i.e.: `sbt run`).
* (4) Profit!

####Testing exercises

All exercises in a library should be tested. A good practice would be for each library to hold a test suite, with a spec for each section. Each spec will contain one test per exercise included in each section. For instance:

```scala
// ...
class SemigroupSpec extends Spec with Checkers {
  def `has a combine operation` = {
    check(
      Test.testSuccess(
        SemigroupSection.semigroupCombine _,
        3 :: List(1, 2, 3, 4, 5, 6) :: Option(3) :: Option(1) :: 67 :: HNil
      )
    )
  }
// ...
}
```

Writing tests for your exercises is easy thanks to the `Test` utility class, which automatically provides a scope with good and bad input data generated by the `ScalaCheck` testing library.

###Publishing the new library

Once you've got your sections and exercises laid out, there are some final steps to see them published in the Scala Exercises website. Let's run them through:

* (1) Publish your library artifacts. It doesn't matter if you use Sonatype, Artifactory, Bintray..., as long as they're publically available.
* (2) Submit a PR to Scala Exercises, including a dependency to your library.
* (3) Your exercises library logo file should be contained inside the classpath of your library (i.e.: in the same location as `MyLibrary.scala`). Your logo should be an SVG file, sporting a white design over a  color background, and a square ratio. If you need help to design your logo, you can submit an issue mentioning any 47 Degrees member. They can lend you a hand!

###Conclusions

The goal of this development guide is to help you to create awesome content for the Scala Exercises project. If you have further doubts, don't hesitate to contact through [Gitter](https://gitter.im/scala-exercises/scala-exercises). And obviously all contributions to this document are welcome!
