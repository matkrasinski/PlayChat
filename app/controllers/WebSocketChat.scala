package controllers

import actors.{ChatActor, ChatManager}
import models.UserChat
import org.apache.pekko.actor.{ActorRef, ActorSystem, Props}
import org.apache.pekko.stream.Materializer
import org.joda.time.DateTimeZone
import play.api.libs.json.{JsObject, Json}
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import repositories.UserChatRepository
import utils.JWTUtils

import java.time.format.{DateTimeFormatter, FormatStyle}
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class WebSocketChat @Inject() (cc: ControllerComponents)
                              (implicit system: ActorSystem,
                               userChatRepository: UserChatRepository,
                               mat: Materializer,
                               executionContext: ExecutionContext
                              ) extends AbstractController(cc) {
  val manager: ActorRef = system.actorOf(Props[ChatManager], "Manager")

  def index: Action[AnyContent] = Action.async { implicit request =>
    request.session.get("jwtToken") match {
      case Some(jwtToken) if JWTUtils.validateJWTToken(jwtToken) =>
        JWTUtils.extractUsername(jwtToken) match {
          case Some(username) =>

            userChatRepository.findAll().flatMap {
              case userChats =>
                Future.successful(Ok(views.html.chatPage.apply(username = username, messages = userChats.toArray)))
              case _ =>
                Future.successful(Ok(views.html.chatPage.apply(username = username, messages = Array.empty)))
            }

          case None =>
            Future.successful(Redirect(routes.AuthenticationController.login))
        }
      case _ =>
        Future.successful(Redirect(routes.AuthenticationController.login))
    }
  }

  def socket: WebSocket = WebSocket.acceptOrResult[String, String] { request =>
    request.session.get("jwtToken") match {
      case Some(jwtToken) if JWTUtils.validateJWTToken(jwtToken) =>
        JWTUtils.extractUsername(jwtToken) match {
          case Some(_) =>
            Future.successful(Right(ActorFlow.actorRef { out =>
              ChatActor.props(userChatRepository, out, manager)
            }))
        }
    }
  }
}
