package nl.toefel.garsson.auth

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.lang.IllegalStateException
import java.security.MessageDigest
import java.time.Duration
import java.time.ZonedDateTime
import java.util.*

class JwtHmacAuthenticator(val signSecret: String, val tokenValidity: Duration) {

    private val hasher = MessageDigest.getInstance("SHA-256")
    private val sigingKey = Keys.hmacShaKeyFor(hasher.digest(signSecret.toByteArray()))

    fun generateJwt(user: User): String {
        return Jwts.builder()
                .setIssuer("garsson-api")
                .setIssuedAt(Date())
                .setNotBefore(Date.from(ZonedDateTime.now().minusMinutes(1).toInstant())) // correction for clock drifts
                .setExpiration(Date.from(ZonedDateTime.now().plusSeconds(tokenValidity.seconds).toInstant()))
                .setAudience("garsson-users")
                .setId(UUID.randomUUID().toString())
                .setSubject(user.name)
                .addClaims(mapOf("roles" to user.roles))
                .signWith(sigingKey)
                .compact()
    }

    fun extractUser(jwt: String): User {
        try {
            val parsedJwt = Jwts.parser()
                    .setSigningKey(sigingKey)
                    .parseClaimsJws(jwt)

            return User(parsedJwt.body.subject, listOf())
        } catch (ex : Exception) {
            throw IllegalStateException("Unauthorized", ex)
        }
    }
}