package io.chapbook.util.auth.jwt

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import play.api.libs.json._
import scala.language.implicitConversions
import scala.util.{ Try, Success, Failure }

import io.chapbook.util.fmt.B64


/** A convenience class for creating and validating JWTs.
 *
 * This class provides methods for generating / validating JWTs, and
 * should be the default interface for working with instances of that
 * class. It is designed to provide a performant alternative to instance
 * methods for common use scenarios.
 *
 * @constructor Creates a new JWTFactory from a private key and algorithm.
 * @param key: A string representing the private key with which to sign / verify JWTs.
 * @param algorithm: The digest algorithm to use, defaults to HS256.
 */
sealed class JWTFactory(key: String, alg: Algorithm = HS256) {
  private val mac: Mac = Mac.getInstance(alg.long)
  mac.init(new SecretKeySpec(key.getBytes, alg.long))

  private val encHeader = B64.encode(
    Json.obj(
      "alg" -> alg.short,
      "typ" -> "JWT"
    ).toString
  )

  /**
   * @param msg: A message to digest with the factory MAC.
   * @return The resulting digest.
   */
  private def sign(msg: String) =
    B64.encode(mac.doFinal(msg.getBytes))

  /**
   * @param token: A JWT to verify against the factory key / algorithm.
   * @return A Boolean representing whether or not the JWT is valid.
   */
  def verify(token: JWT) =
    token.signature == sign(s"${token.encHeader}.${token.encPayload}")

  /**
   * @param payload: A JSON string representing the payload.
   * @return A JWT token.
   */
  def encode(payload: String): JWT = {
    val encPayload = B64.encode(payload)
    val signature = sign(s"${encHeader}.${encPayload}")
    JWT(encHeader, encPayload, signature)
  }

  /**
   * @param payload: A JSON value representing the payload.
   * @return A JWT token.
   */
  def encode(payload: JsValue): JWT =
    encode(Json.stringify(payload))

  /**
   * @param payload: A client-defined Payload object.
   * @return A JWT token.
   */
  def encode[T <: Payload: Format](payload: T): JWT =
    encode(Json.toJson(payload))

  /**
   * @param token: A JWT token.
   * @return On Success, a JSON value representing the payload.
   */
  def decode(token: JWT): Try[JsValue] = 
    if(verify(token)) Try(token.payload)
    else Failure(new SecurityException("Unable to verify token signature."))

  /**
   * @param encToken: A string representing an encoded JWT token.
   * @return On Success, a JSON value representing the payload.
   */
  def decode(encToken: String): Try[JsValue] = JWT.from(encToken) match {
    case Success(token) => decode(token)
    case Failure(thrown) => Failure(thrown)
  }

  /**
   * @tparam A: A concrete subclass of Payload.
   * @param token: A JWT token.
   * @return On Success, an instance of A representing the parsed payload.
   */
  def decodeAs[T <: Payload: Format](token: JWT): Try[T] =
    if(verify(token)) Try(token.payload.as[T])
    else Failure(new SecurityException("Unable to verify token signature."))

  /**
   * @tparam A: A concrete subclass of Payload.
   * @param token: A string representing an encoded JWT token.
   * @return On Success, an instance of A representing the parsed payload.
   */
  def decodeAs[T <: Payload: Format](encToken: String): Try[T] = JWT.from(encToken) match {
    case Success(token) => decodeAs[T](token)
    case Failure(thrown) => Failure(thrown)
  }

  // We really don't need another Try type.
  implicit def jsresult2try[T](result: JsResult[T]): Try[T] = result match {
    case JsSuccess(value, _) => Success(value)
    case JsError(_) => Failure(new IllegalArgumentException("JSON parse error."))
  }
}