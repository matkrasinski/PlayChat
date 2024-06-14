package controllers

import actors.{ChatActor, ChatManager}
import org.apache.pekko.actor.{ActorRef, ActorSystem, Props}
import org.apache.pekko.stream.Materializer
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import utils.JWTUtils

import javax.inject._
import scala.concurrent.Future

@Singleton
class WebSocketChat @Inject() (cc: ControllerComponents)(implicit system: ActorSystem, mat: Materializer) extends AbstractController(cc) {
  val manager: ActorRef = system.actorOf(Props[ChatManager], "Manager")

  def index: Action[AnyContent] = Action { implicit request =>
    request.session.get("jwtToken") match {
      case Some(jwtToken) if JWTUtils.validateJWTToken(jwtToken) =>
        JWTUtils.extractUsername(jwtToken) match {
          case Some(username) =>
            Ok(views.html.chatPage(username))
          case None =>
            Redirect(routes.AuthenticationController.login)
        }
      case _ =>
        Redirect(routes.AuthenticationController.login)
    }
  }

  def socket: WebSocket = WebSocket.acceptOrResult[String, String] { request =>
    request.session.get("jwtToken") match {
      case Some(jwtToken) if JWTUtils.validateJWTToken(jwtToken) =>
        JWTUtils.extractUsername(jwtToken) match {
          case Some(_) =>
            Future.successful(Right(ActorFlow.actorRef { out =>
              ChatActor.props(out, manager)
            }))
        }
    }
  }
}
