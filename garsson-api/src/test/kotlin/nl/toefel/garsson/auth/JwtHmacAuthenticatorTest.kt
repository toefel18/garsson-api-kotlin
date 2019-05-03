package nl.toefel.garsson.auth

import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.security.SignatureException
import io.kotlintest.fail
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.StringSpec
import nl.toefel.garsson.dto.User
import java.time.Duration


class JwtHmacAuthenticatorTest : StringSpec({

    "Generated tokens are parsable again" {
        val auth = JwtHmacAuthenticator("abc", Duration.ofHours(1))
        val jwt = auth.generateJwt(User("frenkie", listOf("player", "midfielder")))

        jwt shouldNotBe null

        val user = auth.extractUser(jwt)
        user.name shouldBe "frenkie"
        user.roles shouldContainExactlyInAnyOrder listOf("player", "midfielder")
    }

    "Parsing token with different signing secret should fail" {
        val authA = JwtHmacAuthenticator("abc", Duration.ofHours(1))
        val authB = JwtHmacAuthenticator("def", Duration.ofHours(1))
        val jwt = authA.generateJwt(User("frenkie", listOf("player", "midfielder")))

        try {
            authB.extractUser(jwt)
            fail("parsing a token signed with different secret should not succeed")
        } catch (ex: SignatureException) {
        }
    }

    "Parsing invalid token" {
        val auth = JwtHmacAuthenticator("abc", Duration.ofHours(1))
        try {
            auth.extractUser("aaaa")
            fail("parsing an invalid token should fail")
        } catch (ex: MalformedJwtException) {
        }
    }
})