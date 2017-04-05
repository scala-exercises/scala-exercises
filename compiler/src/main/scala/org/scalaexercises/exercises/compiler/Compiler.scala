/*
 *  scala-exercises
 *
 *  Copyright 2015-2017 47 Degrees, LLC. <http://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.scalaexercises.compiler

import org.scalaexercises.definitions.{BuildInfo, Library}
import org.scalaexercises.runtime.Timestamp

import scala.reflect.runtime.{universe ⇒ ru}
import cats.Eval
import cats.implicits._
import github4s.Github
import Github._
import github4s.GithubResponses.GHResult
import github4s.free.interpreters.Interpreters
import github4s.free.domain.Commit
import github4s.jvm.Implicits._
import Comments.Mode
import CommentRendering.RenderedComment

import scalaj.http.HttpResponse

class CompilerJava {
  def compile(
      library: AnyRef,
      sources: Array[String],
      paths: Array[String],
      buildMetaInfo: AnyRef,
      baseDir: String,
      targetPackage: String,
      fetchContributors: Boolean
  ): Array[String] = {

    Compiler()
      .compile(
        library.asInstanceOf[Library],
        sources.toList,
        paths.toList,
        buildMetaInfo.asInstanceOf[BuildInfo],
        baseDir,
        targetPackage,
        fetchContributors
      )
      .fold(`🍺` ⇒ throw new Exception(`🍺`), out ⇒ Array(out._1, out._2))
  }
}

case class Compiler() {

  lazy val sourceTextExtractor = new SourceTextExtraction()

  def compile(
      library: Library,
      sources: List[String],
      paths: List[String],
      buildMetaInfo: BuildInfo,
      baseDir: String,
      targetPackage: String,
      fetchContributors: Boolean
  ) = {

    val mirror = ru.runtimeMirror(library.getClass.getClassLoader)
    import mirror.universe._

    val extracted            = sourceTextExtractor.extractAll(sources, paths, baseDir)
    val internal             = CompilerInternal(mirror, extracted)
    val compilationTimestamp = Timestamp.fromDate(new java.util.Date())

    case class LibraryInfo(
        symbol: ClassSymbol,
        comment: RenderedComment.Aux[Mode.Library],
        sections: List[SectionInfo],
        color: Option[String],
        logoPath: String,
        logoData: Option[String] = None,
        owner: String,
        repository: String
    )

    case class ContributionInfo(
        sha: String,
        message: String,
        timestamp: String,
        url: String,
        author: String,
        authorUrl: String,
        avatarUrl: String
    )

    case class SectionInfo(
        symbol: ClassSymbol,
        comment: RenderedComment.Aux[Mode.Section],
        exercises: List[ExerciseInfo],
        imports: List[String] = Nil,
        path: Option[String] = None,
        contributions: List[ContributionInfo] = Nil
    )

    case class ExerciseInfo(
        symbol: MethodSymbol,
        comment: RenderedComment.Aux[Mode.Exercise],
        code: String,
        qualifiedMethod: String,
        packageName: String,
        imports: List[String] = Nil
    )

    def enhanceDocError(path: List[String])(error: String) =
      s"""$error in ${path.mkString(".")}"""

    def maybeMakeLibraryInfo(
        library: Library
    ) =
      for {
        symbol ← internal.instanceToClassSymbol(library)
        symbolPath = internal.symbolToPath(symbol)
        comment ← (internal.resolveComment(symbolPath) >>= Comments.parseAndRender[Mode.Library])
          .leftMap(enhanceDocError(symbolPath) _)
        sections ← checkEmptySectionList(symbol, library) >>= {
          _.sections
            .traverseU(internal.instanceToClassSymbol(_) >>= (symbol ⇒
              maybeMakeSectionInfo(library, symbol)))
        }
      } yield
        LibraryInfo(
          symbol = symbol,
          comment = comment,
          sections = sections,
          color = library.color,
          logoPath = library.logoPath,
          owner = library.owner,
          repository = library.repository
        )

    def checkEmptySectionList(librarySymbol: Symbol, library: Library): Either[String, Library] =
      if (library.sections.isEmpty)
        Either.left(
          s"Unable to create ${librarySymbol.fullName}: A Library object must contain at least one section")
      else
        Either.right(library)

    def fetchContributions(
        owner: String,
        repository: String,
        path: String): List[ContributionInfo] = {
      println(s"Fetching contributions for repository $owner/$repository file $path")
      val contribs = Github(sys.env.lift("GITHUB_TOKEN")).repos
        .listCommits(owner, repository, None, Option(path))

      import github4s.implicits._
      contribs.exec[Eval, HttpResponse[String]](Map.empty).value match {
        case Right(GHResult(result, _, _)) ⇒
          result.collect({
            case Commit(sha, message, date, url, Some(login), Some(avatar_url), Some(author_url)) ⇒
              ContributionInfo(
                sha = sha,
                message = message,
                timestamp = date,
                url = url,
                author = login,
                avatarUrl = avatar_url,
                authorUrl = author_url
              )
          })
        case Left(ex) ⇒ throw ex
      }
    }

    def maybeMakeSectionInfo(
        library: Library,
        symbol: ClassSymbol
    ) = {
      val symbolPath = internal.symbolToPath(symbol)
      val filePath   = extracted.symbolPaths.get(symbol.toString).filterNot(_.isEmpty)
      for {
        comment ← (internal.resolveComment(symbolPath) >>= Comments.parseAndRender[Mode.Section])
          .leftMap(enhanceDocError(symbolPath) _)

        contributions = (if (fetchContributors) filePath else None).fold(
          List.empty[ContributionInfo]
        )(path ⇒ fetchContributions(library.owner, library.repository, path))

        exercises ← symbol.toType.decls.toList
          .filter(symbol ⇒
            symbol.isPublic && !symbol.isSynthetic &&
              symbol.name != termNames.CONSTRUCTOR && symbol.isMethod)
          .map(_.asMethod)
          .filterNot(_.isGetter)
          .traverseU(maybeMakeExerciseInfo)
      } yield
        SectionInfo(
          symbol = symbol,
          comment = comment,
          exercises = exercises,
          imports = Nil,
          path = extracted.symbolPaths.get(symbol.toString),
          contributions = contributions
        )
    }

    def maybeMakeExerciseInfo(
        symbol: MethodSymbol
    ) = {
      val symbolPath = internal.symbolToPath(symbol)
      val pkgName    = symbolPath.headOption.fold("defaultPkg")(pkg ⇒ pkg)
      for {
        comment ← (internal.resolveComment(symbolPath) >>= Comments.parseAndRender[Mode.Exercise])
          .leftMap(enhanceDocError(symbolPath) _)
        method ← internal.resolveMethod(symbolPath)
      } yield
        ExerciseInfo(
          symbol = symbol,
          comment = comment,
          code = method.code,
          packageName = pkgName,
          imports = method.imports,
          qualifiedMethod = symbolPath.mkString(".")
        )
    }

    def oneline(msg: String) = {
      val msg0 = msg.lines.mkString(s"${Console.BLUE}\\n${Console.RESET}")
      // there's a chance that we could put elipses over part of the escaped
      // newline sequence... but oh well
      if (msg0.length <= 100) msg0
      else s"${msg0.take(97)}${Console.BLUE}...${Console.RESET}"
    }

    def dump(libraryInfo: LibraryInfo) {
      println(s"Found library ${libraryInfo.comment.name}")
      println(s" description: ${oneline(libraryInfo.comment.description)}")
      libraryInfo.sections.foreach { sectionInfo ⇒
        println(s" with section ${sectionInfo.comment.name}")
        println(s"  path: ${sectionInfo.path}")
        println(s"  description: ${sectionInfo.comment.description.map(oneline)}")
        sectionInfo.exercises.foreach { exerciseInfo ⇒
          println(s"  with exercise ${exerciseInfo.symbol}")
          println(s"   description: ${exerciseInfo.comment.description.map(oneline)}")
        }
      }
    }

    // leaving this around, for debugging
    def debugDump(libraryInfo: LibraryInfo) {
      println("~ library")
      println(s" • symbol        ${libraryInfo.symbol}")
      println(s" - name          ${libraryInfo.comment.name}")
      println(s" - description   ${oneline(libraryInfo.comment.description)}")
      libraryInfo.sections.foreach { sectionInfo ⇒
        println(" ~ section")
        println(s"  • symbol        ${sectionInfo.symbol}")
        println(s"  - name          ${sectionInfo.comment.name}")
        println(s"  - description   ${sectionInfo.comment.description.map(oneline)}")
        sectionInfo.exercises.foreach { exerciseInfo ⇒
          println("  ~ exercise")
          println(s"   • symbol        ${exerciseInfo.symbol}")
          println(s"   - description   ${exerciseInfo.comment.description.map(oneline)}")
        }
      }
    }

    val treeGen = TreeGen[mirror.universe.type](mirror.universe)

    def generateTree(libraryInfo: LibraryInfo): (TermName, Tree) = {

      val (sectionTerms, sectionAndExerciseTrees) =
        libraryInfo.sections.map { sectionInfo ⇒
          val (exerciseTerms, exerciseTrees) =
            sectionInfo.exercises.map { exerciseInfo ⇒
              treeGen.makeExercise(
                libraryName = libraryInfo.comment.name,
                name = internal.unapplyRawName(exerciseInfo.symbol.name),
                description = exerciseInfo.comment.description,
                code = exerciseInfo.code,
                qualifiedMethod = exerciseInfo.qualifiedMethod,
                packageName = exerciseInfo.packageName,
                imports = exerciseInfo.imports,
                explanation = exerciseInfo.comment.explanation
              )
            }.unzip

          val (contributionTerms, contributionTrees) =
            sectionInfo.contributions.map { contributionInfo ⇒
              treeGen.makeContribution(
                sha = contributionInfo.sha,
                message = contributionInfo.message,
                timestamp = contributionInfo.timestamp,
                url = contributionInfo.url,
                author = contributionInfo.author,
                authorUrl = contributionInfo.authorUrl,
                avatarUrl = contributionInfo.avatarUrl
              )
            }.unzip

          val (sectionTerm, sectionTree) =
            treeGen.makeSection(
              libraryName = libraryInfo.comment.name,
              name = sectionInfo.comment.name,
              description = sectionInfo.comment.description,
              exerciseTerms = exerciseTerms,
              imports = sectionInfo.imports,
              path = sectionInfo.path,
              contributionTerms = contributionTerms
            )

          (sectionTerm, sectionTree :: exerciseTrees ++ contributionTrees)
        }.unzip

      val allDependencies: List[String] = {
        val libraryAsDependency =
          s"${buildMetaInfo.organization}:${buildMetaInfo.name}_${buildMetaInfo.scalaVersion
            .substring(0, 4)}:${buildMetaInfo.version}"
        libraryAsDependency :: buildMetaInfo.libraryDependencies.toList
      }

      val (buildInfoTerm, buildInfoTree) =
        treeGen.makeBuildInfo(
          name = libraryInfo.comment.name,
          resolvers = buildMetaInfo.resolvers.toList,
          libraryDependencies = allDependencies
        )

      val (libraryTerm, libraryTree) = treeGen.makeLibrary(
        name = libraryInfo.comment.name,
        description = libraryInfo.comment.description,
        color = libraryInfo.color,
        logoPath = libraryInfo.logoPath,
        logoData = libraryInfo.logoData,
        sectionTerms = sectionTerms,
        owner = libraryInfo.owner,
        repository = libraryInfo.repository,
        timestamp = compilationTimestamp,
        buildInfoT = buildInfoTerm
      )

      libraryTerm → treeGen.makePackage(
        packageName = targetPackage,
        trees = libraryTree :: (sectionAndExerciseTrees.flatten :+ buildInfoTree)
      )

    }

    maybeMakeLibraryInfo(library)
      .map(generateTree)
      .map { case (TermName(kname), v) ⇒ s"$targetPackage.$kname" → showCode(v) }

  }

  private case class CompilerInternal(
      mirror: ru.Mirror,
      sourceExtracted: SourceTextExtraction#Extracted
  ) {
    import mirror.universe._

    def instanceToClassSymbol(instance: AnyRef) =
      Either
        .catchNonFatal(mirror.classSymbol(instance.getClass))
        .leftMap(e ⇒ s"Unable to get module symbol for $instance due to: $e")

    def resolveComment(path: List[String]) /*: Either[String, Comment] */ =
      Either.fromOption(
        sourceExtracted.comments.get(path).map(_.comment),
        s"""Unable to retrieve doc comment for ${path.mkString(".")}"""
      )

    def resolveMethod(path: List[String]): Either[String, SourceTextExtraction#ExtractedMethod] =
      Either.fromOption(
        sourceExtracted.methods.get(path),
        s"""Unable to retrieve code for method ${path.mkString(".")}"""
      )

    def symbolToPath(symbol: Symbol): List[String] = {
      def process(symbol: Symbol): List[String] = {
        lazy val owner = symbol.owner
        unapplyRawName(symbol.name) match {
          case `ROOT`                      ⇒ Nil
          case `EMPTY_PACKAGE_NAME_STRING` ⇒ Nil
          case `ROOTPKG_STRING`            ⇒ Nil
          case value if symbol != owner    ⇒ value :: process(owner)
          case _                           ⇒ Nil
        }
      }
      process(symbol).reverse
    }

    private[compiler] def unapplyRawName(name: Name): String = name match {
      case TermName(value) ⇒ value
      case TypeName(value) ⇒ value
    }

    private lazy val EMPTY_PACKAGE_NAME_STRING = unapplyRawName(termNames.EMPTY_PACKAGE_NAME)
    private lazy val ROOTPKG_STRING            = unapplyRawName(termNames.ROOTPKG)
    private lazy val ROOT                      = "<root>" // can't find an accessible constant for this

  }

}
