package controllers

import models.{ExerciseEvaluation, Section, Category, Exercise}
import play.api.libs.json.{Reads, Json, Writes}

trait JsonFormats {

  implicit val exerciseWrites: Writes[Exercise] = Json.writes[Exercise]

  implicit val categoryWrites: Writes[Category] = Json.writes[Category]

  implicit val sectionWrites: Writes[Section] = Json.writes[Section]

  implicit val exerciseEvaluationReads : Reads[ExerciseEvaluation] = Json.reads[ExerciseEvaluation]

}

object JsonFormats extends JsonFormats
