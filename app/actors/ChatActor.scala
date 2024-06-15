package actors

import actors.ChatActor.SendMessage
import models.UserChatBuilder
import org.apache.pekko.actor.{Actor, ActorRef, Props}
import play.api.libs.json._
import repositories.UserChatRepository
import utils.JWTUtils

import javax.inject.Inject
import scala.util.{Failure, Success, Try}

class ChatActor @Inject() (userChatRepository: UserChatRepository, out : ActorRef, manager : ActorRef) extends Actor {
  manager ! ChatManager.NewChatter(self)

  def receive: Receive = {
    case msg: String =>
      val jsonTry: Try[JsValue] = Try(Json.parse(msg))

      jsonTry match {
        case Success(jsonMsg) =>
          val token = (jsonMsg \ "jwtToken").asOpt[String]
          val formattedToken = token.get.strip().substring("PLAY_SESSION=".length)

          val (valid, username) = JWTUtils.validateNestedJWTToken(formattedToken)
          if (!valid) {
            out ! Json.obj("action" -> "refresh").toString()
          } else {
            var message = (jsonMsg \ "msg").get.toString()
            message = message.substring(1, message.length - 1)

            val newChatMessage = UserChatBuilder()
              .withUsername(username)
              .withMessage(message)
              .build()

            userChatRepository.create(newChatMessage)

            manager ! ChatManager.Message(
              Json.obj(
                "msg" -> message,
                "username" -> username,
                "datetime" -> newChatMessage.dateTime.get.toString()
              ).toString())
          }
        case Failure(_) => manager ! ChatManager.Message(msg)
      }
    case SendMessage(msg) => out ! msg
  }
}

object ChatActor {
  def props(userChatRepository: UserChatRepository, out: ActorRef, manager: ActorRef): Props = Props(new ChatActor(userChatRepository, out, manager))

  case class SendMessage(msg: String)
}