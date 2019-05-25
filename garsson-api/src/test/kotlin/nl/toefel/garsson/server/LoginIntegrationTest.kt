package nl.toefel.garsson.server

import io.kotlintest.extensions.TestListener
import io.kotlintest.specs.FunSpec
import nl.toefel.garsson.ApplicationRestTest
import nl.toefel.garsson.ApplicationTest
import nl.toefel.garsson.dto.LoginCredentials
import nl.toefel.garsson.dto.SuccessfulLoginResponse
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.startsWith
import org.hamcrest.MatcherAssert.assertThat


class LoginIntegrationTest : ApplicationRestTest, FunSpec() {
    override fun listeners(): List<TestListener> = listOf(ApplicationTest)

    val email = "test-user@domain.com"

    init {
        test("Login should provide valid JWT") {
            val credentials = LoginCredentials(email, "chicken")
            val responseBody = post("/api/v1/login", credentials)
                .statusCode(200)
                .header("Authorization", startsWith("Bearer ey"))
                .body("token", startsWith("ey"))
                .extract().body().`as`(SuccessfulLoginResponse::class.java)

            validateJwt(responseBody.token, ApplicationTest.config.jwtSigningSecret, email)
        }
    }

    private fun validateJwt(token: String, secret: String, email: String) {
        val user = ApplicationTest.server?.auth?.extractUser(token)!!

        assertThat(user.name, equalTo(email))
    }
}