package services

import services.messages._

import scala.concurrent.{Future, ExecutionContext}

trait SectionServices {

  def fetchSections(): Future[FetchSectionsResponse]

}

class SectionServicesImpl(implicit val executionContext: ExecutionContext) extends SectionServices {

  override def fetchSections(): Future[FetchSectionsResponse] = {
    Future.successful(FetchSectionsResponse(sections = Seq.empty))
  }


}