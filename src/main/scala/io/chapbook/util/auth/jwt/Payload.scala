package io.chapbook.util.auth.jwt

import java.util.UUID

/** An abstract representation of a JWT payload.
 *
 * Subclass Payload to define a project specific JSON payload.
 * Subclass individual traits to provide RFC-defined claims. 
 */
abstract class Payload

object Claims {
  trait Exp extends Payload {
    val exp: Long
    if(exp < System.currentTimeMillis) throw new SecurityException("Token expired.")
  }

  trait Nbf extends Payload {
    val nbf: Long
    if(nbf > System.currentTimeMillis) throw new SecurityException("Token not yet valid.")
  }
}