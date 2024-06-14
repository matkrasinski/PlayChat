package repositories

import models.UserChat
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.{Cursor, ReadPreference}
import reactivemongo.api.bson.compat._

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class UserChatRepository @Inject() (
                                 implicit executionContext: ExecutionContext,
                                 reactiveMongoApi: ReactiveMongoApi
                               ) {
  def collection: Future[BSONCollection] = reactiveMongoApi.database.map(_.collection("userChats"))

  def findAll(limit: Int = 100): Future[Seq[UserChat]] = {
    collection.flatMap(
      _.find(BSONDocument(), Option.empty[UserChat])
        .cursor[UserChat](ReadPreference.Primary)
        .collect[Seq](limit, Cursor.FailOnError[Seq[UserChat]]())
    )
  }

  def findUserChats(userId: String, limit: Int = 10): Future[Seq[UserChat]] = {
    collection.flatMap(
      _.find(BSONDocument("$or" ->
          List(
            BSONDocument("user1" -> userId),
            BSONDocument("user2" -> userId))),
          Option.empty[UserChat])
        .cursor[UserChat](ReadPreference.Primary)
        .collect[Seq](limit, Cursor.FailOnError[Seq[UserChat]]())
    )
  }

  def findUsersChat(userChat: UserChat): Future[Option[UserChat]] = {
    collection.flatMap(
      _.find(BSONDocument("$or" ->
          List(
            BSONDocument("user1" -> userChat.user1, "user2" -> userChat.user2),
            BSONDocument("user1" -> userChat.user2, "user2" -> userChat.user1))),
          Option.empty[UserChat])
        .one[UserChat]
    )
  }

  def createUserChat(userChat: UserChat) : Future[WriteResult] = {
    findUsersChat(userChat).flatMap {
      case Some(_) => Future.failed(new Exception("UserChat already exists"))
      case None => collection.flatMap(_.insert(ordered = false).one(userChat.copy()))
    }
  }
}

