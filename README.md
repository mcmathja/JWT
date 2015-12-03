# jwt

jwt provides token-based claims authorization based on the [JSON Web Token](http://jwt.io/) standard. It is intended to provide efficient, simple production and parsing of JWTs. Unlike other libraries, we require that the signature mechanisms used to decode each JWT be specified in advance by server, rather than within the JWT itself, improving performance and avoiding certain classes of vulnerabilities.

# Usage

```scala
import io.chapbook.util.auth.jwt._

// Define your token payload
case class SessionPayload(
  my_user_id: UUID,
  my_nonce: String,
  exp: Long
) extends Payload with Exp

// Create a new JWTFactory with some secret key:
val authenticator = new JWTFactory("secret")

// Encode a payload into a JWT
val sessionToken = authenticator.encode(SessionPayload(userId, nonce, exp))

// Decode an incoming JWT
authenticator.decodeAs[SessionPayload](sessionToken) match {
  case Success(sessionPayload) => doSomethingWith(sessionPayload.my_user_id)
  // Token is either invalid or has expired.
  case Failure(e) => throw new AuthorizationException("Bad token!")
}
```
