package models

import org.joda.time.DateTime
import reactivemongo.bson.BSONObjectID


case class UserChatBuilder private (_id: BSONObjectID = null,
                                 datetime: DateTime = new DateTime(),
                                 username: String = null,
                                 message: String = "") {

  def withId(id: BSONObjectID): UserChatBuilder = {
    copy(_id = id)
  }

  def withDateTime(datetime: DateTime) : UserChatBuilder = {
    copy(datetime = datetime)
  }

  def withUsername(username : String ): UserChatBuilder = {
    copy(username = username)
  }

  def withMessage(message: String) : UserChatBuilder = {
    copy(message = message)
  }

  def build() = new UserChat(
      _id = Option(_id),
      dateTime = Some(datetime),
      userName = username,
      message = message
    )
}