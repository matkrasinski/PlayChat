package controllers

import models.UserChat
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import org.apache.pekko.stream.javadsl.Flow
import org.joda.time.DateTime
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.Futures.whenReady
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.http.Status.{OK, SEE_OTHER, SWITCHING_PROTOCOLS}
import play.api.test.Helpers.{GET, contentAsString, defaultAwaitTimeout, redirectLocation, status, stubControllerComponents}
import play.api.test.{FakeRequest, Injecting}
import repositories.UserChatRepository

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class WebSocketChatTest extends PlaySpec with GuiceOneAppPerTest with Injecting with MockitoSugar with BeforeAndAfterEach {

  private val testToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImV4cCI6MzYwMTcxODQ1MDAyNiwiaWF0IjoxNzE4NDUzNjI2fQ.KngIMpDjd_Bh0rTKm62yscFbE8M6XHt3rGXmxayfr4M"

  var userChatRepository: UserChatRepository = null
  var chatHistory: Seq[UserChat] = null
  var webSocketChat: WebSocketChat = null

  override def beforeEach(): Unit = {
    super.beforeEach()
    userChatRepository = mock[UserChatRepository]
    val userChat = mock[UserChat]
    when(userChat.message).thenReturn("hello")
    when(userChat.userName).thenReturn("testuser")
    when(userChat.dateTime).thenReturn(Some(new DateTime()))
    chatHistory = Seq(userChat)
    webSocketChat = new WebSocketChat(stubControllerComponents())(ActorSystem(), userChatRepository, global)
  }

  "WebSocketChat GET" should {

    "chats history is shown on join" in {
      when(userChatRepository.findAll()).thenReturn(Future.successful(chatHistory))

      val chatRequest = FakeRequest(GET, "/chat")
        .withSession("jwtToken" -> testToken)
      val result = webSocketChat.index()(chatRequest)

      status(result) mustBe OK
      contentAsString(result) must include("testuser - hello")
    }

    "chats history is shown not shown due to invalid token" in {
      when(userChatRepository.findAll()).thenReturn(Future.successful(chatHistory))

      val chatRequest = FakeRequest(GET, "/chat")
        .withSession("jwtToken" -> testToken.replace("a", "b"))
      val result = webSocketChat.index()(chatRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("/login")
    }

    "chats empty history is shown on join when db empty" in {
      when(userChatRepository.findAll()).thenReturn(Future.successful(Seq.empty))

      val chatRequest = FakeRequest(GET, "/chat")
        .withSession("jwtToken" -> testToken)
      val result = webSocketChat.index()(chatRequest)

      status(result) mustBe OK
      contentAsString(result) must include("<textarea id=\"chat-area\" rows=\"15\" cols=\"80\" disabled>\n\n</textarea>")
    }

  }
}
