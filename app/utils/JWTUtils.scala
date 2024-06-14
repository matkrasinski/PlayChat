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

object JWTUtils {

  // Secret key for signing the JWT token
  // NEED TO BE SET TO MAKE JWT WORK CORRECTLY
  private val secretKey = "yJhbGciOiJIUzI1NiJ9.eyJkYXRhIjp7Imp3dFRva2VuIjoiZXlKaGJHY2lPaUpJVXpJMU5pSjkuZXlKemRXSWlPaUoxYzJWeUlpd2laWGh3SWpveE56RTRNemsxTURrNExDSnBZWFFpT2pFM01UZ3pPVE15T1RoOS5iRFFuaUM5MHRaUHNhX2tWZGVwR0o3OVhFRjNKRDVVUGVzUks0X29kWEtvIiwiY3NyZlRva2VuIjoiMTczZjllZjJmNzhjMDc3Y2MyYTM3MjRhNDMzYmQxNGQxYTRmNjE5Yy0xNzE4MzkzMjk4NzgwLWY3ZmE5OTg2OWU2MDJlZDMwYTJjMDQ0YSJ9LCJuYmYiOjE3MTgzOTMyOTgsImlhdCI6MTcxODM5MzI5OH0.M-H1YrP0IcKSjI7JDhf1MRZSExd299OC_Kg21DP99ks"

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
