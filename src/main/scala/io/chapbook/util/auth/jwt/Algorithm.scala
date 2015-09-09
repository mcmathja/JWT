package io.chapbook.util.auth.jwt

/** A represenation of a hashing algorithm.
 *
 * Primarily an easy mechanism to translate between different naming standards.
 *
 * @constructor Creates a new Algorithm instance from its short and long names.
 * @param short: The short-form name - e.g., for use in JWT payloads.
 * @param long: The long-form name - e.g., for use with javax.crypto.
 */
sealed abstract class Algorithm(val short: String, val long: String)
case object HS256 extends Algorithm("HS256", "HmacSHA256")
case object HS384 extends Algorithm("HS384", "HmacSHA384")
case object HS512 extends Algorithm("HS512", "HmacSHA512")