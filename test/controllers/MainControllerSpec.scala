package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test._
import play.api.test.Helpers._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 *
 * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
 */
class MainControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  "MainController GET" should {

    "render the index page from a new instance of controller" in {
      val controller = new MainController(stubControllerComponents())
      val mainPage = controller.index().apply(FakeRequest(GET, "/"))

      redirectLocation(mainPage).get mustBe "/chat"
      status(mainPage) mustBe SEE_OTHER
    }

    "render the index page from the application" in {
      val controller = inject[MainController]
      val mainPage = controller.index().apply(FakeRequest(GET, "/"))

      redirectLocation(mainPage).get mustBe "/chat"
      print(contentAsString(mainPage))
      status(mainPage) mustBe SEE_OTHER
    }

    "render the index page from the router" in {
      val request = FakeRequest(GET, "/")
      val mainPage = route(app, request).get

      redirectLocation(mainPage).get mustBe "/chat"
      status(mainPage) mustBe SEE_OTHER
    }
  }
}
