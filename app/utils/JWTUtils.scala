package utils

import com.nimbusds.jose._
import com.nimbusds.jose.crypto.{MACSigner, MACVerifier}
import com.nimbusds.jwt.{JWTClaimsSet, SignedJWT}
import play.api.libs.json.Json

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import scala.util.Try
import io.github.cdimascio.dotenv.Dotenv

object JWTUtils {

  // Secret key for signing the JWT token
  // NEED TO BE SET TO MAKE JWT WORK CORRECTLY
  private val dotenv = Dotenv.load()
  private val secretKey = dotenv.get("JWT_SECRET")

  // Generate JWT token
  def generateJWTToken(username: String): String = {
    val now = new Date()
    val expirationTime = Date.from(Instant.now().plus(30, ChronoUnit.MINUTES))
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
  def validateJWTToken(jwtToken: String): Boolean = {
    val signedJWT = SignedJWT.parse(jwtToken)
    val verifier = new MACVerifier(secretKey)

    signedJWT.verify(verifier) && !isExpired(jwtToken)
  }

  def isExpired(jwtToken: String): Boolean = {
    Try {
      val signedJWT = SignedJWT.parse(jwtToken)
      val claimsSet = signedJWT.getJWTClaimsSet
      val expirationTime = claimsSet.getExpirationTime.toInstant

      expirationTime.isBefore(Instant.now())
    }.getOrElse(true)
  }

  def validateNestedJWTToken(nestedJwt: String): (Boolean, String) = {
    val nested = SignedJWT.parse(nestedJwt)
    val nestedJson = nested.getPayload.toJSONObject.get("data")

    val token = extractJWTToken(nestedJson.toString)

    (validateJWTToken(token), extractUsername(token).orNull)
  }

  def extractJWTToken(input: String) = {
    val keyValuePairs = input
      .stripPrefix("{")
      .stripSuffix("}")
      .split(", ")
      .map { pair =>
        val Array(key, value) = pair.split("=")
        (key.trim, value.trim)
      }.toMap

    val json = Json.obj(
      "jwtToken" -> keyValuePairs("jwtToken"),
      "csrfToken" -> keyValuePairs("csrfToken")
    )

    val jwtToken = (json \ "jwtToken").as[String]
    // In case csrf token would be validated
    // val csrfToken = (json \ "csrfToken").as[String]

    jwtToken
  }


  def extractUsername(jwtToken: String): Option[String] = {
    try {
      val signedJWT = SignedJWT.parse(jwtToken)
      val claims = signedJWT.getJWTClaimsSet
      Option(claims.getSubject)
    } catch {
      case _: Exception => None
    }
  }

}
