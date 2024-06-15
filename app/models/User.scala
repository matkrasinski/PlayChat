package models

import play.api.libs.json.{Format, Json}
import reactivemongo.play.json._
import reactivemongo.bson.BSONObjectID
import reactivemongo.bson._



case class User (_id: Option[BSONObjectID],
                 username: String,
                 password: String)

object User {
  implicit val fmt: Format[User] = Json.format[User]
  implicit object UserBSONReader extends BSONDocumentReader[User] {
    override def read(doc: BSONDocument): User = {
      User(
        doc.getAs[BSONObjectID]("_id"),
        doc.getAs[String]("username").get,
        doc.getAs[String]("password").get
      )
    }
  }
  implicit object UserBSONWriter extends BSONDocumentWriter[User] {
    override def write(user: User): BSONDocument = {
      BSONDocument(
        "_id" -> user._id,
        "username" -> user.username,
        "password" -> user.password
      )
    }
  }
}