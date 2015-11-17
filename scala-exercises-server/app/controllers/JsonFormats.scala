package controllers

import models.{ExerciseEvaluation, Section, Category, Exercise}
import play.api.libs.json.{Reads, Json, Writes}
import services.messages.{CreateFollowupResponse, CreateFollowupRequest}
import shared.Followup

trait JsonFormats {

  implicit val followupWrites: Writes[Followup] = Json.writes[Followup]

  implicit val createFollowupRequestReads: Reads[CreateFollowupRequest] = Json.reads[CreateFollowupRequest]

  implicit val createFollowupRequestWrites: Writes[CreateFollowupResponse] = Json.writes[CreateFollowupResponse]

  implicit val exerciseWrites: Writes[Exercise] = Json.writes[Exercise]

  implicit val categoryWrites: Writes[Category] = Json.writes[Category]

  implicit val sectionWrites: Writes[Section] = Json.writes[Section]

  implicit val exerciseEvaluationReads : Reads[ExerciseEvaluation] = Json.reads[ExerciseEvaluation]

}

object JsonFormats extends JsonFormats
