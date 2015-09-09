package io.chapbook.util.auth.jwt

import play.api.libs.json._
import scala.language.implicitConversions
import scala.util.{ Try, Success, Failure }

import io.chapbook.util.fmt.B64

/** A representation of a JWT.
 *
 * @constructor Creates a new JWT instance from encoded components.
 * @param encHeader: The Base64 encoded header of the JWT.
 * @param encPayload: The Base64 encoded payload of the JWT.
 * @param signature: The Base64 encoded, signed digest of the header and payload.
 */
case class JWT(encHeader: String, encPayload: String, signature: String) {
  lazy val header = Json.parse(B64.decode(encHeader))
  lazy val payload = Json.parse(B64.decode(encPayload))
  override def toString = s"${encHeader}.${encPayload}.${signature}"
}

object JWT {
  /**
   * @param from: An encoded token string to convert into a JWT.
   * @return A new JWT instance.
   */
  def from(encToken: String): Try[JWT] = encToken split '.' match {
    case Array(encHeader, encPayload, signature) =>
      Success(JWT(encHeader, encPayload, signature))
    case _ => Failure(new IllegalArgumentException("Invalid token."))
  }

  /**
   * @param token: A JWT instance.
   * @return The string 
   */
  implicit def jwt2String(token: JWT): String = token.toString

  implicit object jwtFormat extends Format[JWT] {
    def reads(json: JsValue): JsResult[JWT] = json.validate[String] match {
      case JsSuccess(encToken, _) => JWT.from(encToken) match {
        case Success(jwt) => JsSuccess(jwt)
        case Failure(e) => JsError("Invalid token format.")
      }
      case JsError(_) => JsError("Invalid token format.")
    }
    def writes(jwt: JWT) = JsString(jwt.toString)
  }
}