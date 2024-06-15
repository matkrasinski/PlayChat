package controllers

import models.UserBuilder
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import repositories.UserRepository
import utils.JWTUtils.generateJWTToken

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import utils.PasswordUtils

@Singleton
class AuthenticationController @Inject()(implicit executionContext: ExecutionContext,
                                         userRepository: UserRepository,
                                         cc: ControllerComponents) extends AbstractController(cc) {

  // Define a form mapping for the username and password
  val loginForm: Form[(String, String)] = Form(
    tuple(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    )
  )

  val registerForm: Form[(String, String, String)] = Form(
    tuple(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText,
      "confirmPassword" -> nonEmptyText
    )
  )

  // Action to display the login form
  def login: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.login(loginForm))
  }

//   Action to handle form submission
  def authenticate: Action[AnyContent] = Action.async { implicit request =>
    loginForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.login(formWithErrors)))
      },
      userData => {
        val (username, password) = userData

        userRepository.findByUsername(username).flatMap {
          case Some(user) =>
            if (user.username == user.username && PasswordUtils.checkPassword(password, user.password)) {
              val token = generateJWTToken(username)

              Future.successful(Redirect(routes.WebSocketChat.index)
                .withSession("jwtToken" -> token))
            } else {
              Future.successful(Unauthorized(views.html.login(loginForm.withGlobalError("Invalid username or password"))))
            }
          case None => Future.successful(Unauthorized(views.html.login(loginForm.withGlobalError("User does not exist"))))
        }
      }
    )
  }

  def register: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.register(registerForm))
  }

  def createUser : Action[AnyContent] = Action.async {implicit request => {
    registerForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.register(formWithErrors)))
      },
      userData => {
        val (username, password, confirmPassword) = userData
        if (password != confirmPassword) {
          Future.successful(BadRequest(views.html.register(registerForm.withGlobalError("Password and ConfirmPassword fields should be the same"))))
        } else {
          val newUser = UserBuilder()
            .withUsername(username)
            .withPassword(password)
            .build()

          userRepository.create(newUser).map { _ =>
            Redirect(routes.AuthenticationController.login)
          }.recover {
            case e: Exception =>
              InternalServerError(views.html.register(registerForm.withGlobalError("Error creating user: " + e.getMessage)))
          }
        }
      }
    )
  }}
}
