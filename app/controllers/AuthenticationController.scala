package controllers

import javax.inject._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import com.nimbusds.jose.{JWSAlgorithm, JWSHeader, JWSObject, JWSSigner, Payload}
import com.nimbusds.jose.crypto.{MACSigner, MACVerifier}
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import play.api.libs.json.Json

import java.util.Date
import java.time.Instant
import java.time.temporal.ChronoUnit

@Singleton
class AuthenticationController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  // Define a form mapping for the username and password
  val loginForm: Form[(String, String)] = Form(
    tuple(
      "username" -> text,
      "password" -> text
    )
  )

  val registerForm: Form[(String, String, String)] = Form(
    tuple(
      "username" -> text,
      "password" -> text,
      "confirmPassword" -> text
    )
  )

  // Secret key for signing the JWT token
  private val secretKey = "your-secret-key"

  // Action to display the login form
  def login: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.login(loginForm))
  }

  // Action to handle form submission
  def authenticate: Action[AnyContent] = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.login(formWithErrors))
      },
      userData => {
        // Perform authentication logic here, e.g. check username and password against a database
        val (username, password) = userData
        if (username == "admin" && password == "password123") {
          val token = generateJWTToken(username)
          Ok(Json.obj("token" -> token))
        } else {
          Unauthorized("Invalid username or password")
        }
      }
    )
  }

  def register: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.register(registerForm))
  }

  // Action to handle secure endpoint with JWT token
  def secureEndpoint: Action[AnyContent] = Action { implicit request =>
    request.headers.get("Authorization") match {
      case Some(jwtToken) =>
        if (validateJWTToken(jwtToken)) {
          Ok("Welcome!")
        } else {
          Unauthorized("Invalid token")
        }
      case None =>
        Unauthorized("No token provided")
    }
  }

  // Generate JWT token
  private def generateJWTToken(username: String): String = {
    val now = new Date()
    val expirationTime = Date.from(Instant.now().plus(1, ChronoUnit.HOURS))
    val claims = new JWTClaimsSet.Builder()
      .subject(username)
      .issueTime(now)
      .expirationTime(expirationTime)
      .build()
    val signer: JWSSigner = new MACSigner(secretKey)
    val jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS256), new Payload(claims.toJSONObject))
    jwsObject.sign(signer)
    jwsObject.serialize()
  }

  // Validate JWT token
  private def validateJWTToken(jwtToken: String): Boolean = {
    val signedJWT = SignedJWT.parse(jwtToken)
    val verifier = new MACVerifier(secretKey)
    signedJWT.verify(verifier)
  }
}
