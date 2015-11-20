package services

import models.FollowupModel
import services.messages._

import scala.concurrent.{Future, ExecutionContext}

trait FollowupServices {

  def create(request: CreateFollowupRequest): Future[CreateFollowupResponse]

  def retrieve(request: RetrieveFollowupRequest): Future[RetrieveFollowupResponse]

}

class FollowupServicesImpl(implicit val executionContext: ExecutionContext) extends FollowupServices {

  override def create(request: CreateFollowupRequest): Future[CreateFollowupResponse] = {

    val result = for {
      followup <- FollowupModel.store.create(
        login = request.login,
        section = request.section,
        category = request.category,
        status = request.status)
    } yield CreateFollowupResponse(followup = followup)

    result recover {
      case e => throw new Exception(s"Followup creation error: ${e.getMessage}")
    }
  }

  override def retrieve(request: RetrieveFollowupRequest): Future[RetrieveFollowupResponse] = {

    val result = for {
      followups <- FollowupModel.store.retrieve(login = request.login)
    } yield RetrieveFollowupResponse(followups = followups.groupBy(_.section).mapValues(_.size))

    result recover {
      case e => throw new Exception(s"Followup creation error: ${e.getMessage}")
    }
  }

}