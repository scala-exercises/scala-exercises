package models

import scala.concurrent.Future
import shared.Followup
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global

object FollowupModel {
  val store: FollowupStore = FollowupSlickStore
}

trait FollowupStore {

  def create(text: String): Future[Followup]

}

object FollowupSlickStore extends FollowupStore {

  import play.api.db.DB
  import slick.driver.PostgresDriver.api._

  class Followups(tag: Tag) extends Table[Followup](tag, "followups"){
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)
    def text = column[String]("text")
    def * = (id, text) <> (Followup.tupled, Followup.unapply)
  }

  private def db: Database = Database.forDataSource(DB.getDataSource())

  val followups = TableQuery[Followups]

  override def create(text: String): Future[Followup] = {
    db.run{
      (followups returning followups.map(_.id) into ((followup, id) => followup.copy(id=id))) += Followup(
      id = None,
      text = text)
    }
  }

}