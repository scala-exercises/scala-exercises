package controllers

import java.util.UUID

import services.{FollowupServicesImpl, UserServiceImpl}
import services.messages.{RetrieveFollowupRequest, GetUserByLoginRequest}
import services.parser.ExercisesService
import utils.OAuth2
import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

object Application extends Controller {

  def index = Action.async { implicit request =>

    val userService = new UserServiceImpl
    val followupService = new FollowupServicesImpl

    val oauth2 = new OAuth2(Play.current)
    val callbackUrl = utils.routes.OAuth2.callback(None, None).absoluteURL()
    val logoutUrl = utils.routes.OAuth2.logout().absoluteURL()
    val scope = "user"
    val state = UUID.randomUUID().toString
    val redirectUrl = oauth2.getAuthorizationUrl(callbackUrl, scope, state)

    val sections = ExercisesService.sections

    request.session.get("oauth-token").map { token =>

      for {
        userRes <- userService.getUserByLogin(GetUserByLoginRequest(login = request.session.get("user").getOrElse("")))
        user = userRes.user
        login = user.map(_.login).getOrElse("")
        followupsRes <- followupService.retrieve(RetrieveFollowupRequest(login))
      } yield Ok(views.html.templates.home(user = user, sections = sections, followups = followupsRes.followups))

    }.getOrElse {
      Future.successful(Ok(views.html.templates.home(user = None, sections = sections, redirectUrl = Option(redirectUrl))).withSession("oauth-state" -> state))
    }

  }


}

