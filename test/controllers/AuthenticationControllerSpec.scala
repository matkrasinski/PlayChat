package controllers

import models.{User, UserBuilder}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.Assertion
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test.Helpers._
import play.api.test._
import reactivemongo.api.commands.WriteResult
import repositories.UserRepository
import utils.JWTUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthenticationControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting with MockitoSugar {

  private val controller: AuthenticationController = new AuthenticationController()(global, mock[UserRepository], stubControllerComponents())

  private def renderPage(endpoint: String): Assertion = {
    val page = controller.login().apply(FakeRequest(GET, endpoint))
    status(page) mustBe OK
    contentType(page) mustBe Some("text/html")
  }

  "AuthenticationController GET" should {

    "render the login page from a new instance of controller" in renderPage("/login")

    "render the register page from a new instance of controller" in renderPage("/register")
  }

  "AuthenticationController POST" should {

    "authenticate a user with valid credentials" in {
      val userRepository = mock[UserRepository]
      val user = UserBuilder().withUsername("testuser").withPassword("password").build();
      when(userRepository.findByUsername("testuser")).thenReturn(Future.successful(Some(user)))

      val controller = new AuthenticationController()(global, userRepository, stubControllerComponents())
      val request = FakeRequest(POST, "/authenticate")
        .withFormUrlEncodedBody("username" -> "testuser", "password" -> "password")

      val loginResult = controller.authenticate().apply(request)

      status(loginResult) mustBe SEE_OTHER
      redirectLocation(loginResult) mustBe Some(routes.WebSocketChat.index.url)
      val jwtToken = session(loginResult).data.values.head
      JWTUtils.validateJWTToken(jwtToken) mustBe true
    }

    "reject a user with invalid credentials" in {
      val userRepository = mock[UserRepository]
      val user = UserBuilder().withUsername("testuser").withPassword("password").build();
      when(userRepository.findByUsername("testuser")).thenReturn(Future.successful(Some(user)))

      val controller = new AuthenticationController()(global, userRepository, stubControllerComponents())
      val request = FakeRequest(POST, "/authenticate")
        .withFormUrlEncodedBody("username" -> "testuser", "password" -> "wrongpassword")

      val loginResult = controller.authenticate().apply(request)

      status(loginResult) mustBe UNAUTHORIZED
    }

    "reject a user that does not exist" in {
      val userRepository = mock[UserRepository]
      when(userRepository.findByUsername("unknownuser")).thenReturn(Future.successful(None))

      val controller = new AuthenticationController()(global, userRepository, stubControllerComponents())
      val request = FakeRequest(POST, "/authenticate")
        .withFormUrlEncodedBody("username" -> "unknownuser", "password" -> "password")

      val loginResult = controller.authenticate().apply(request)

      status(loginResult) mustBe UNAUTHORIZED
    }

    "register a user with valid credentials" in {
      val userRepository = mock[UserRepository]
      when(userRepository.create(any[User])).thenReturn(Future.successful(mock[WriteResult]))

      val controller = new AuthenticationController()(global, userRepository, stubControllerComponents())
      val request = FakeRequest(POST, "/createUser")
        .withFormUrlEncodedBody("username" -> "newuser", "password" -> "password", "confirmPassword" -> "password")

      val registerResult = controller.createUser().apply(request)

      status(registerResult) mustBe SEE_OTHER
      redirectLocation(registerResult) mustBe Some(routes.AuthenticationController.login.url)
    }

    "reject registration with mismatched passwords" in {
      val userRepository = mock[UserRepository]

      val controller = new AuthenticationController()(global, userRepository, stubControllerComponents())
      val request = FakeRequest(POST, "/createUser")
        .withFormUrlEncodedBody("username" -> "newuser", "password" -> "password", "confirmPassword" -> "differentpassword")

      val registerResult = controller.createUser().apply(request)

      status(registerResult) mustBe BAD_REQUEST
    }
  }
}
