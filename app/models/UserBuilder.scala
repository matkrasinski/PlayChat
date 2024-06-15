package models


import reactivemongo.bson.BSONObjectID
import utils.PasswordUtils


case class UserBuilder private (
                                 _id: BSONObjectID = null,
                                 username: String = "",
                                 password: String = ""
                               ) {
  def withId(BSONObjectID: BSONObjectID): UserBuilder = {
    copy(_id = BSONObjectID)
  }

  def withUsername(username: String): UserBuilder = {
    copy(username = username)
  }

  def withPassword(password: String): UserBuilder = {
    copy(password = PasswordUtils.hashPassword(password))
  }

  def build() = new User (
      _id = Option(_id),
      username = username,
      password = password
  )
}