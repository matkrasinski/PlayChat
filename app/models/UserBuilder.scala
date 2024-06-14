package models


import reactivemongo.bson.BSONObjectID


case class UserBuilder private (
                                 _id: BSONObjectID = null,
                                 username: String = "",
                                 email: String = "",
                                 password: String = ""
                               ) {
  def withId(BSONObjectID: BSONObjectID): UserBuilder = {
    copy(_id = BSONObjectID)
  }

  def withUsername(username: String): UserBuilder = {
    copy(username = username)
  }

  def withEmail(email: String): UserBuilder = {
    copy(email = email)
  }

  def withPassword(password: String): UserBuilder = {
    copy(password = password)
  }

  def build() = new User (
      _id = Option(_id),
      username = username,
      email = email,
      password = password
  )
}