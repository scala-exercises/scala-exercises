package controllers

import services.messages.{RetrieveFollowupRequest, CreateFollowupRequest}
import services.FollowupServicesImpl
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.Future

object FollowupController extends Controller with JsonFormats {

  val followupService = new FollowupServicesImpl

  def create = Action.async(BodyParsers.parse.json) { implicit request =>
    request.body.validate[CreateFollowupRequest].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("status" ->"KO", "message" -> JsError.toJson(errors))))
      },
      followup => {
        followupService.create(followup).map(r => Ok(Json.toJson(r)))
      })
  }

  def retrieve(login: String) = Action.async { implicit request =>
    followupService.retrieve(RetrieveFollowupRequest(login)).map(r => Ok(Json.toJson(r)))
  }

}
