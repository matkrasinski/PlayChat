package actors

import org.apache.pekko.actor.{Actor, ActorRef, Props}
import play.api.libs.json._
import utils.JWTUtils

import scala.util.{Failure, Success, Try}

class ChatActor(out: ActorRef, manager: ActorRef) extends Actor {
  manager ! ChatManager.NewChatter(self)

  def receive: Receive = {
    case msg: String =>
      val jsonTry: Try[JsValue] = Try(Json.parse(msg))

      jsonTry match {
        case Success(jsonMsg) =>
          val token = (jsonMsg \ "jwtToken").asOpt[String]
          val formattedToken = token.get.strip().substring("PLAY_SESSION=".length)

          val valid = JWTUtils.validateNestedJWTToken(formattedToken)
          if (!valid) {
            out ! Json.obj("action" -> "refresh").toString()
          } else {
            val message = (jsonMsg \ "msg").get.toString()

            out ! message.substring(1, message.length - 1)
          }
        case Failure(_) =>
          out ! msg
      }
  }
}

object ChatActor {
  def props(out: ActorRef, manager: ActorRef): Props = Props(new ChatActor(out, manager))

  case class SendMessage(msg: String)
}