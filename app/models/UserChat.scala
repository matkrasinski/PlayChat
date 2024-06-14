package models

import play.api.libs.json.{Format, Json}
import reactivemongo.play.json._
import reactivemongo.bson.{BSONObjectID, _}


case class UserChat (_id: Option[BSONObjectID], user1: String, user2: String, messages: Array[String])

object UserChat {
  implicit val fmt: Format[UserChat] = Json.format[UserChat]
  implicit object UserChatBSONReader extends BSONDocumentReader[UserChat] {
    override def read(doc: BSONDocument): UserChat = {
      UserChat(
        doc.getAs[BSONObjectID]("_id"),
        doc.getAs[String]("user1").get,
        doc.getAs[String]("user2").get,
        doc.getAs[Array[String]]("messages").get
      )
    }
  }
  implicit object UserChatBSONWriter extends BSONDocumentWriter[UserChat] {
    override def write(userChat: UserChat): BSONDocument = {
      BSONDocument(
        "_id" -> userChat._id,
        "user1" -> userChat.user1,
        "user2" -> userChat.user2,
        "messages" -> userChat.messages
      )
    }
  }
}