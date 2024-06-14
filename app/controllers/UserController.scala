package controllers

import models.User
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import reactivemongo.bson.BSONObjectID
import repositories.UserRepository

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class UserController @Inject()(
                                 implicit executionContext: ExecutionContext,
                                 val userRepository: UserRepository,
                                 val controllerComponents: ControllerComponents) extends BaseController {

  def findAll():Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    userRepository.findAll().map {
      users => Ok(Json.toJson(users))
    }
  }

  def findOne(id:String):Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    val objectIdTryResult = BSONObjectID.parse(id)
    objectIdTryResult match {
      case Success(objectId) => userRepository.findOne(objectId).map {
        user => Ok(Json.toJson(user))
      }
      case Failure(_) => Future.successful(BadRequest("Cannot parse the user id"))
    }
  }

  def create():Action[JsValue] = Action.async(controllerComponents.parsers.json) { implicit request => {
    request.body.validate[User].fold(
      _ => Future.successful(BadRequest("Cannot parse request body")),
      user =>
        userRepository.create(user).map {
          _ => Created(Json.toJson(user))
        }
    )
  }}

  def update(id: String):Action[JsValue]  = Action.async(controllerComponents.parsers.json) { implicit request => {
    request.body.validate[User].fold(
      _ => Future.successful(BadRequest("Cannot parse request body")),
      user =>{
        val objectIdTryResult = BSONObjectID.parse(id)
        objectIdTryResult match {
          case Success(objectId) => userRepository.update(objectId, user).map {
            result => Ok(Json.toJson(result.ok))
          }
          case Failure(_) => Future.successful(BadRequest("Cannot parse the user id"))
        }
      }
    )
  }}

  def delete(id: String):Action[AnyContent]  = Action.async { implicit request => {
    val objectIdTryResult = BSONObjectID.parse(id)
    objectIdTryResult match {
      case Success(objectId) => userRepository.delete(objectId).map {
        _ => NoContent
      }
      case Failure(_) => Future.successful(BadRequest("Cannot parse the user id"))
    }
  }}

}