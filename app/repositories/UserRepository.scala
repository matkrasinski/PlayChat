package repositories

import models.User
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.{Cursor, ReadPreference}
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import reactivemongo.api.bson.compat._

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class UserRepository @Inject() (implicit executionContext: ExecutionContext, reactiveMongoApi: ReactiveMongoApi) {
  def collection: Future[BSONCollection] = reactiveMongoApi.database.map(_.collection("users"))

  def findAll(limit: Int = 100): Future[Seq[User]] = {
    collection.flatMap(
      _.find(BSONDocument(), Option.empty[User])
        .cursor[User](ReadPreference.Primary)
        .collect[Seq](limit, Cursor.FailOnError[Seq[User]]())
    )
  }
  def findOne(id: BSONObjectID): Future[Option[User]] = {
    collection.flatMap(_.find(BSONDocument("_id" -> id), Option.empty[User]).one[User])
  }

  def findByUsername(username: String): Future[Option[User]] = {
    collection.flatMap(_.find(BSONDocument("username" -> username), Option.empty[User]).one[User])
  }


  def create(user: User): Future[WriteResult] = {
    collection.flatMap(_.insert(ordered = false)
      .one(user.copy()))
  }

  def update(id: BSONObjectID, user: User):Future[WriteResult] = {
    collection.flatMap(
      _.update(ordered = false).one(BSONDocument("_id" -> id),
        user.copy())
    )
  }

  def delete(id: BSONObjectID):Future[WriteResult] = {
    collection.flatMap(
      _.delete().one(BSONDocument("_id" -> id), Some(1))
    )
  }
}

