package repositories

import models.{User, UserChat}
import org.joda.time.DateTime
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.{Cursor, ReadPreference}
import reactivemongo.bson.{BSONDocument, BSONDocumentHandler, BSONObjectID}
import reactivemongo.api.bson.compat._

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class UserChatRepository @Inject() (implicit executionContext: ExecutionContext, reactiveMongoApi: ReactiveMongoApi) {
  def collection: Future[BSONCollection] = reactiveMongoApi.database.map(_.collection("userChats"))

  def create(userChat: UserChat): Future[WriteResult] = {
    collection.flatMap(_.insert(ordered = true).one(userChat.copy(dateTime = Some(new DateTime()))))
  }

  def findByMessage(messagePart : String): Future[Option[UserChat]] = {
    collection.flatMap(_.find(
      BSONDocument("message" -> s"/.*$messagePart.*/i"),
      Option.empty[UserChat])
      .one[UserChat])
  }

  def delete(id: BSONObjectID) : Future[WriteResult] = {
    collection.flatMap(_.delete()
      .one(BSONDocument("_id" -> id), Some(1)))
  }

  def findAll(limit: Int = 100): Future[Seq[UserChat]] = {
    collection.flatMap(c =>
        c.find(BSONDocument(), Option.empty[UserChat])
        .sort(BSONDocument("datetime" -> 1))
        .cursor[UserChat](ReadPreference.Primary)
        .collect[Seq](limit, Cursor.FailOnError[Seq[UserChat]]())
    )
  }
}

