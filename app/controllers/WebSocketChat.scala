package controllers

import actors.{ChatActor, ChatManager}
import org.apache.pekko.actor.{ActorRef, ActorSystem, Props}
import org.apache.pekko.stream.Materializer
import play.api.libs.streams.ActorFlow
import play.api.mvc._

import javax.inject._

@Singleton
class WebSocketChat @Inject() (cc: ControllerComponents)(implicit system: ActorSystem, mat: Materializer) extends AbstractController(cc) {
  val manager: ActorRef = system.actorOf(Props[ChatManager], "Manager")

  def index: Action[AnyContent] = Action { implicit request => Ok(views.html.chatPage())}

  def socket: WebSocket = WebSocket.accept[String, String] { _ =>
    ActorFlow.actorRef { out =>
      ChatActor.props(out, manager)
    }
  }
}
