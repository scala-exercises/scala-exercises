package com.fortysevendeg.exercises
package compiler

import scala.reflect.api.Universe
import scala.reflect.runtime.{ universe ⇒ ru }
import scala.reflect.internal.util.BatchSourceFile

import cats.data.Xor
import cats.std.all._
import cats.syntax.flatMap._
import cats.syntax.traverse._

object CompilerJava {
  def compile(library: exercise.Library, sources: Array[String], targetPackage: String): String = {
    Compiler().compile(library, sources.toList, targetPackage)
      .fold(error ⇒ throw new Exception(error), `🍺` ⇒ `🍺`)
  }
}

case class Compiler() {

  lazy val docCommentFinder = new DocCommentFinder()

  def compile(library: exercise.Library, sources: List[String], targetPackage: String) = {

    val mirror = ru.runtimeMirror(library.getClass.getClassLoader)
    import mirror.universe._

    val internal = CompilerInternal(mirror, docCommentFinder.findAll(sources))

    case class LibraryInfo(
      symbol:   ClassSymbol,
      comment:  DocParser.ParsedLibraryComment,
      sections: List[SectionInfo]
    )

    case class SectionInfo(
      symbol:    ClassSymbol,
      comment:   DocParser.ParsedSectionComment,
      exercises: List[ExerciseInfo]
    )

    case class ExerciseInfo(
      symbol:  MethodSymbol,
      comment: DocParser.ParsedExerciseComment
    )

    def enhanceDocError(symbol: Symbol)(error: String) =
      s"""$error in ${internal.symbolToPath(symbol).mkString(".")}"""

    def maybeMakeLibraryInfo(
      library: exercise.Library
    ) = for {
      symbol ← internal.instanceToClassSymbol(library)
      comment ← (internal.resolveComment(symbol) >>= DocParser.parseLibraryDocComment)
        .leftMap(enhanceDocError(symbol))
      sections ← library.sections.toList
        .map(internal.instanceToClassSymbol(_) >>= maybeMakeSectionInfo)
        .sequenceU
    } yield LibraryInfo(
      symbol = symbol,
      comment = comment,
      sections = sections
    )

    def maybeMakeSectionInfo(
      symbol: ClassSymbol
    ) = for {
      comment ← (internal.resolveComment(symbol) >>= DocParser.parseSectionDocComment)
        .leftMap(enhanceDocError(symbol))
      exercises ← symbol.toType.decls.toList
        .filter(symbol ⇒
          symbol.isPublic && !symbol.isSynthetic &&
            symbol.name != termNames.CONSTRUCTOR && symbol.isMethod)
        .map(_.asMethod)
        .filterNot(_.isGetter)
        .map(maybeMakeExerciseInfo)
        .sequenceU
    } yield SectionInfo(
      symbol = symbol,
      comment = comment,
      exercises = exercises
    )

    def maybeMakeExerciseInfo(
      symbol: MethodSymbol
    ) = for {
      comment ← (internal.resolveComment(symbol) >>= DocParser.parseExerciseDocComment)
        .leftMap(enhanceDocError(symbol))
    } yield ExerciseInfo(
      symbol = symbol,
      comment = comment
    )

    // keeping this, as it's very useful for debugging
    // TODO: properly send this output to SBT?
    def dump(libraryInfo: LibraryInfo) {
      println("~ library")
      println(s" • symbol        ${libraryInfo.symbol}")
      println(s" - name          ${libraryInfo.comment.name}")
      println(s" - description   ${libraryInfo.comment.description}")
      libraryInfo.sections.foreach { sectionInfo ⇒
        println(" ~ section")
        println(s"  • symbol        ${sectionInfo.symbol}")
        println(s"  - name          ${sectionInfo.comment.name}")
        println(s"  - description   ${sectionInfo.comment.description}")
        sectionInfo.exercises.foreach { exerciseInfo ⇒
          println("  ~ exercise")
          println(s"   • symbol        ${exerciseInfo.symbol}")
          println(s"   - name          ${exerciseInfo.comment.name}")
          println(s"   - description   ${exerciseInfo.comment.description}")
        }
      }
    }

    val treeGen = TreeGen[mirror.universe.type](mirror.universe)

    def generateTree(libraryInfo: LibraryInfo): Tree = {

      val (sectionTerms, sectionAndExerciseTrees) =
        libraryInfo.sections.map { sectionInfo ⇒
          val (exerciseTerms, exerciseTrees) =
            sectionInfo.exercises
              .map { exerciseInfo ⇒
                treeGen.makeExercise(
                  name = exerciseInfo.comment.name,
                  description = exerciseInfo.comment.description,
                  code = Some("// TODO (compiler support)!"),
                  explanation = exerciseInfo.comment.explanation
                )
              }.unzip

          val (sectionTerm, sectionTree) =
            treeGen.makeSection(
              name = sectionInfo.comment.name,
              description = Some(sectionInfo.comment.description),
              exerciseTerms = exerciseTerms
            )

          (sectionTerm, sectionTree :: exerciseTrees)
        }.unzip

      val (libraryTerm, libraryTree) = treeGen.makeLibrary(
        name = libraryInfo.comment.name,
        description = libraryInfo.comment.description,
        color = "purple", // TODO: where should this get defined?
        sectionTerms = sectionTerms
      )

      treeGen.makePackage(
        packageName = targetPackage,
        trees = libraryTree :: sectionAndExerciseTrees.flatten
      )

    }

    maybeMakeLibraryInfo(library)
      .map(generateTree)
      .map(showCode(_))

  }

  private case class CompilerInternal(mirror: ru.Mirror, docComments: Map[List[String], String]) {
    import mirror.universe._

    def instanceToClassSymbol(instance: AnyRef) =
      Xor.catchNonFatal(mirror.classSymbol(instance.getClass))
        .leftMap(e ⇒ s"Unable to get module symbol for $instance due to: $e")

    def resolveComment(symbol: Symbol): Xor[String, String] = {
      val path = symbolToPath(symbol)
      Xor.fromOption(
        docComments.get(path),
        s"""Unable to retrieve doc comment for ${path.mkString(".")}"""
      )
    }

    def symbolToPath(symbol: Symbol): List[String] = {
      def process(symbol: Symbol): List[Name] = {
        val owner = symbol.owner
        symbol.name match {
          case TypeName(`EMPTY_PACKAGE_NAME_STRING`) ⇒ Nil
          case TypeName(`ROOTPKG_STRING`)            ⇒ Nil
          case _ if symbol != owner                  ⇒ symbol.name :: process(owner)
          case _                                     ⇒ Nil
        }
      }
      process(symbol).reverse.collect {
        case TermName(value) ⇒ value
        case TypeName(value) ⇒ value
      }
    }

    // Not positive why I had to do this...
    private lazy val EMPTY_PACKAGE_NAME_STRING = termNames.EMPTY_PACKAGE_NAME match {
      case TermName(value) ⇒ value
    }

    private lazy val ROOTPKG_STRING = termNames.ROOTPKG match {
      case TermName(value) ⇒ value
    }

  }

}
