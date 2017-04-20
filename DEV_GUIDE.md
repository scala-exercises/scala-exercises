# Developer guide

## Contributing new content

In addition to including features and fixes, you can contribute new content to Scala Exercises! The platform is designed in a way that creating new exercises is pretty straightforward. Currently there are several libraries available (such as the Scala standard library, Cats, and Shapeless), and more are to be included soon. If you're interested in creating a new exercises library, you'll find all the needed steps in this document.

### Overview

The [scala-exercises](https://github.com/scala-exercises) organization holds different repositories for both the core project ([scala-exercises](https://github.com/scala-exercises/scala-exercises)), and each of the main scala-exercises libraries (such as [exercises-stdlib](https://github.com/scala-exercises/exercises-stdlib) or [exercises-cats](https://github.com/scala-exercises/exercises-cats)). The core project is currently built on one single module. The content you can provide to the platform doesn't necessarily need to be in the previously mentioned organization, but for them to appear inside the main hosted `scala-exercises`, they need to be published in a public artifact repository like Sonatype or Maven-central. All the steps, from creating the content library to the publication process, are described in the following sections.

You can also find a [bare-bones template](https://github.com/scala-exercises/exercises-template) set up for you, containing a basic library with one section. Feel free to fork it as a foundation for your own library.

### Content creation

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

#### Setting up a library project

First of all, before starting to develop your library, you need to add a couple of dependencies and configurations to your project. These are:

1. Add a dependency to the `sbt-exercises` plugin in the `plugins.sbt` file of your project (replacing `version` for the proper one):

```scala
resolvers += Resolver.sonatypeRepo("snapshots")
addSbtPlugin("org.scala-exercises" % "sbt-exercise" % version, "0.13", "2.10")
```

2. Activate the plugin in the `build.sbt` file of your library, for instance in the following way:

```scala
lazy val contentLibrary = (project in file("."))
.enablePlugins(ExerciseCompilerPlugin)
// further configurations for your project go here
```

3. Extra dependencies should be added to the `build.sbt` file of your library (replacing `version` for the proper one):

```scala
// Add the needed resolvers if you don't have them already set up:
resolvers ++= Seq(
    Resolver.sonatypeRepo("snapshots"),
    Resolver.sonatypeRepo("releases")
)

// Necessary dependencies:
libraryDependencies ++= Seq(
// ...
    "org.scala-exercises" %% "exercise-compiler" % version
    "org.scala-exercises" %% "definitions" % version
//...
)
```

#### Creating a new content library

Each content library is an independent SBT module working as a library, which should be published as an artifact for it to be included in Scala Exercises. You'll also need to set up a GitHub repository for your libraries, at least containing one file, as repository information is needed by the Scala Exercises compiler to fetch their contributors. From now on, we'll assume you've already set up a new project for your exercises library, and we'll focus on the specifics on how to layout the content itself.

First of all you need to set up the library by creating a new Scala `object` that contains basic details about it. This `object` must subclass from `org.scalaexercises.definitions.Library`. Here you can find a template for this object:

```scala
package package_name // (1)

import org.scalaexercises.definitions._

/** This line should describe your library shortly, and will appear as its description in the main Scala Exercises website. // (2)
  *
  * @param name library_name // (3)
  */
object MyLibrary extends Library {
  override def owner = "owner" // (4)
  override def repository = "github_repository" // (5)

  override def color = Some("#HEX_COLOR") // (6)

  // (7)
  override def sections = List(
    SectionA,
    SectionB,
    ...
    SectionN
  )

  // (8)
  override def logoPath = "logo_path"
}
```

Examining each of these points:

1. A package name for your exercises library.
2. Inside the first [ScalaDoc](https://wiki.scala-lang.org/display/SW/Syntax) block, you define the description of the library as it'll appear in the list of the available libraries.
3. Also, this [ScalaDoc](https://wiki.scala-lang.org/display/SW/Syntax) block defines the title of the library. It'll also be used as the path suffix for its URL. If you need to use spaces, use an underscore symbol (i.e.: "STD_lib")
4. The existing owner username or organization for the content on GitHub.
5. The name of the repository that will contain the library. This should lead to an existing repository with actual content within. The owner (4) and repository (5) fields must resolve to a real GitHub repository (https://github.com/owner/repository).
6. Color in hexadecimal code format that will identify the library in the website. This value is optional, although it's recommended to pick one.
7. A list that contains every section you want to include in this library. Every item defined here is a Scala object containing the exercises per section.
8. The filename of the library logo (without extension) to be displayed in Scala Exercises, in SVG format.

#### Creating a new section

As previously mentioned, exercises are organized in sections that will be based on a Scala `object`. A good practice would be to have each section in a separate Scala file. So the basic flow to create new exercises would be:

1. Define the section `object`. The process for defining this is in the next section.
2. Include a reference to this object in the `sections` list in the library `object` described in the previous section.

What does a section look like?

```scala
import org.scalatest._  // (1)
import org.scalaexercises.definitions._

/** @param name section_title // (2)
*/
object SectionA extends FlatSpec with Matchers with Section { // (3)
    // exercises definition // (4)
}
```

1. You will need to set up a [ScalaTest](http://www.scalatest.org/) dependency in your library. If you're using SBT you will not need to set it up for the `test` scope (note that ScalaTest is also been used in the main source of your library).
2. As when defining the library, you need to use [ScalaDoc](https://wiki.scala-lang.org/display/SW/Syntax) to define the name of the section. Use an underscore symbol to include spaces.
3. Each section `object` should inherit from `org.scalaexercises.definitions.Section`. `FlatSpec` and `Matchers` are traits from the [ScalaTest](http://www.scalatest.org/) library, the framework Scala Exercises uses to check the results.
4. Inside the section `object` you can start defining the exercises, as it'll be shown in the following section.

#### Creating new exercises

The content for each exercise is defined in a [ScalaDoc](https://wiki.scala-lang.org/display/SW/Syntax) block, with some specific conventions.

```scala
/** = Title = // (1)
*
* Text describing background about the exercise, can be as long as needed. // (2)
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

1. You can define titles in the documentation for your library wrapping it inside a pair of equal signs (=), defining a [ScalaDoc](https://wiki.scala-lang.org/display/SW/Syntax) heading.
2. The documentation itself can be split up in several paragraphs, depending on your needs. You can use backquotes (`) to highlight a specific item or a code reference.
3. Code that illustrates your exercises should be surrounded by triple curly braces.
4. Code that defines your exercises is contained in a normal Scala function below the [ScalaDoc](https://wiki.scala-lang.org/display/SW/Syntax) containing the documentation.

Exercise functions (as described in (4)) should receive one parameter per placeholder that users will fill in the user interface, and return `Unit`. You can include as many placeholders as needed, but you must have a minimum of one. Inside the function, you can include nested functions that your exercise may need to work, and a set of [ScalaTest](http://www.scalatest.org/) asserts checking the user's inputs.

#### Known limitations

Scala Exercises is still in development, so in some situations you might potentially find some limitations while laying out your exercises. Some of them are described as follows:

1. Currently every [ScalaDoc](https://wiki.scala-lang.org/display/SW/Syntax) block in an exercise must be associated with a function, which in practice it means that all sections must end with an exercise. This is currently referenced in issue [#556](https://github.com/scala-exercises/scala-exercises/issues/556).
2. Scala Exercises only supports exercises with placeholders to be filled by the user. Potentially it'll allow writing complete code snippets from the users in order to complete an exercise.

#### Local development

In order to see and debug your exercises in your local environment, you need to publish them locally and include them as a dependency for your local instance of the `scala-exercises` project. Here are the steps to achieve this:

1. Compile your exercises (i.e.: `sbt compile`). This is needed for each change, as the publication process can fail otherwise.
2. Publish your excersises locally (i.e.: `sbt publishLocal`).
3. Note that you need to include a dependency to your content library in your local `scala-exercises` project. Your dependency fields should match the values included in the `build.sbt` file of your library (i.e.: `organization`, `name`, and `version`).
4. Run [Scala Exercises](https://github.com/scala-exercises/scala-exercises) in your local development environment (i.e.: `sbt run`).
5. Profit!

#### Testing exercises

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

### Publishing the new library

Once you've got your sections and exercises laid out, there are some final steps to see them published in the Scala Exercises website:

1. Publish your library artifacts. It doesn't matter if you use Sonatype, Artifactory, Bintray..., as long as they're publically available.
2. Submit a pull request to Scala Exercises to include a dependency to your library.
3. You should include an exercise library logo file, contained inside the classpath of your library, in the same location as your library. The logo should be an SVG file, sporting a white design over a color background, and a square ratio. If you need help to design your logo, you can submit an issue mentioning any 47 Degrees member. They can lend you a hand!

### Conclusions

The goal of this development guide is to help you to create awesome content for the Scala Exercises project. If you have further doubts, don't hesitate to contact through [Gitter](https://gitter.im/scala-exercises/scala-exercises). And obviously all contributions to this document are welcome!
