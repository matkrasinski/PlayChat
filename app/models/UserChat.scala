package models

import org.joda.time.DateTime
import play.api.libs.json.{Format, Json}
import reactivemongo.play.json._
import reactivemongo.bson.BSONObjectID
import reactivemongo.bson._
import play.api.libs.json.JodaWrites._
import play.api.libs.json.JodaReads._


case class UserChat (_id: Option[BSONObjectID],
                      userName: String,
                      dateTime: Option[DateTime],
                      message: String)

object UserChat {
  implicit val fmt: Format[UserChat] = Json.format[UserChat]
  implicit object UserChatBSONReader extends BSONDocumentReader[UserChat] {
    override def read(doc: BSONDocument): UserChat = {
      UserChat(
        doc.getAs[BSONObjectID]("_id"),
        doc.getAs[String]("username").get,
        doc.getAs[BSONDateTime]("datetime").map(dt => new DateTime(dt.value)),
        doc.getAs[String]("message").get
      )
    }
  }
  implicit object UserChatBSONWriter extends BSONDocumentWriter[UserChat] {
    override def write(userChat: UserChat): BSONDocument = {
      BSONDocument(
        "_id" -> userChat._id,
        "username" -> userChat.userName,
        "datetime" -> userChat.dateTime.map(date => BSONDateTime(date.getMillis)),
        "message" -> userChat.message
      )
    }
  }

//  implicit object UserChatUpdateBSONWriter extends BSONDocumentWriter[UserChat] {
//    override def write(update : UserChat): BSONDocument = {
//      BSONDocument(
//        "$set" -> BSONDocument(
//          "messages" -> update.messages
//        )
//      )
//    }
//  }
}