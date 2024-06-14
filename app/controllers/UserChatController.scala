package controllers

import javax.inject._
import models.UserChat
import play.api.libs.json.{Json, JsError}
import play.api.mvc._
import repositories.UserChatRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserChatController @Inject() (
                                     cc: ControllerComponents,
                                     userChatRepository: UserChatRepository
                                   )(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def index: Action[AnyContent] = Action.async { implicit _: Request[AnyContent] =>
    userChatRepository.findAll().map { userChats =>
      Ok(views.html.userChatsPage(userChats))
    }
  }

  def getAllUserChats: Action[AnyContent] = Action.async {
    userChatRepository.findAll().map { userChats =>
      Ok(Json.toJson(userChats))
    }
  }

  def getUserChats(userId: String): Action[AnyContent] = Action.async {
    userChatRepository.findUserChats(userId).map { userChats =>
      Ok(Json.toJson(userChats))
    }
  }

  def createUserChat: Action[AnyContent] = Action.async { request =>
    request.body.asJson.map { json =>
      json.validate[UserChat].fold(
        errors => Future.successful(BadRequest(JsError.toJson(errors))),
        userChat => {
          userChatRepository.createUserChat(userChat).map { _ =>
            Created(Json.toJson(userChat))
          }.recover {
            case ex: Exception => Conflict(Json.obj("message" -> ex.getMessage))
          }
        }
      )
    }.getOrElse(Future.successful(BadRequest("Invalid JSON")))
  }
}
