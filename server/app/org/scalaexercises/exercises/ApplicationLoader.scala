/*
 *  scala-exercises
 *
 *  Copyright 2015-2019 47 Degrees, LLC. <http://www.47deg.com>
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

package org.scalaexercises.exercises

import _root_.controllers._
import cats.effect.{Blocker, IO}
import com.typesafe.config.ConfigFactory
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import org.scalaexercises.algebra.exercises.ExerciseOps
import org.scalaexercises.algebra.github.GithubOps
import org.scalaexercises.algebra.progress._
import org.scalaexercises.algebra.user.UserOps
import org.scalaexercises.exercises.controllers._
import org.scalaexercises.exercises.services.handlers._
import play.api.ApplicationLoader.Context
import play.api._
import play.api.cache.caffeine.CaffeineCacheComponents
import play.api.db.evolutions.{DynamicEvolutions, EvolutionsComponents}
import play.api.db.{DBComponents, HikariCPComponents}
import play.api.http.DefaultFileMimeTypes
import play.api.libs.ws._
import play.api.libs.ws.ahc.AhcWSClient
import play.api.mvc.EssentialFilter
import play.http.DefaultHttpFilters
import router.Routes

import scala.concurrent.Future

class ExercisesApplicationLoader extends ApplicationLoader {
  def load(context: Context) = {
    val mode = context.environment.mode.toString.toLowerCase
    new Components(
      context.copy(
        initialConfiguration = context.initialConfiguration
          ++ Configuration(ConfigFactory.load(s"application.$mode.conf"))
      )).application
  }
}

class Components(context: Context)
    extends BuiltInComponentsFromContext(context)
    with DBComponents
    with CaffeineCacheComponents
    with EvolutionsComponents
    with HikariCPComponents {

  applicationEvolutions.start()

  override lazy val dynamicEvolutions: DynamicEvolutions = new DynamicEvolutions

  val trans = (for {
    ec <- ExecutionContexts.fixedThreadPool[IO](32)
    cs = IO.contextShift(ec)
    blocker <- Blocker[IO]
  } yield
    Transactor.fromDataSource[IO](
      dbApi.database("default").dataSource,
      ec,
      blocker.blockingContext)(implicitly, cs)).allocated

  implicit val wsClient: WSClient = AhcWSClient()

  implicit val components = controllerComponents

  implicit val mode = application.mode

  def generateRoutes(implicit T: Transactor[IO]): Routes = {
    implicit val exerciseOps: ExerciseOps[IO]            = new ExerciseOpsHandler[IO](application)
    implicit val userOps: UserOps[IO]                    = new UserOpsHandler[IO]
    implicit val userProgressOps: UserProgressOps[IO]    = new UserProgressOpsHandler[IO]
    implicit val githubOps: GithubOps[IO]                = new GithubOpsHandler[IO]
    implicit val userProgress: UserExercisesProgress[IO] = new UserExercisesProgress[IO]

    val applicationController = new ApplicationController(application, components)(defaultCacheApi)
    val exercisesController   = new ExercisesController(application, components)
    val userController        = new UserController(application.mode, components)
    val oauthController =
      new OAuthController(application.mode, application.configuration, components)
    val userProgressController = new UserProgressController(components)
    val loaderIOController =
      new LoaderIOController(application.mode, application.configuration, components)
    val sitemapController = new SitemapController(application.mode, components)

    new Routes(
      httpErrorHandler,
      sitemapController,
      loaderIOController,
      applicationController,
      userController,
      exercisesController,
      assets,
      oauthController,
      userProgressController
    )
  }

  val assets = new _root_.controllers.Assets(
    httpErrorHandler,
    new DefaultAssetsMetadata(
      AssetsConfiguration.fromConfiguration(configuration, application.mode),
      environment.resource(_),
      new DefaultFileMimeTypes(httpConfiguration.fileMimeTypes)
    )
  )

  lazy val router = trans.map(tran => generateRoutes(tran._1)).unsafeRunSync()

  applicationLifecycle.addStopHook({ () ⇒
    trans.flatMap(_._2).unsafeRunSync()
    Future(wsClient.close())
  })

  override def httpFilters: Seq[EssentialFilter] = Seq.empty
}
