package nl.toefel.garsson.server

import io.kotlintest.extensions.TestListener
import io.kotlintest.specs.FunSpec
import nl.toefel.garsson.ApplicationRestTest
import nl.toefel.garsson.ApplicationTest
import nl.toefel.garsson.dto.LoginCredentials
import nl.toefel.garsson.dto.SuccessfulLoginResponse
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat


class LoginIntegrationTest : ApplicationRestTest, FunSpec() {
    override fun listeners(): List<TestListener> = listOf(ApplicationTest)

    val email = "test@dummy.nl"

    init {
        test("Valid login should provide valid JWT in response and Authorization header") {
            val credentials = LoginCredentials(email, "test")
            val responseBody = post("/api/v1/login", credentials)
                .statusCode(200)
                .header("Authorization", startsWith("Bearer ey"))
                .contentType("application/json")
                .body("token", startsWith("ey"))
                .extract().body().`as`(SuccessfulLoginResponse::class.java)

            validateJwt(responseBody.token, email)
        }

        test("Unknown user should issue 401 with user not found error") {
            val credentials = LoginCredentials("non-existent@blaat.nl", "test")
            post("/api/v1/login", credentials)
                .statusCode(401)
                .header("Authorization", nullValue())
                .contentType("application/json")
                .body("status", equalTo(401))
                .body("message", equalTo("user not found"))
                .body("uri", equalTo("/api/v1/login"))
        }

        test("Invalid password should issue 401 with invalid password error") {
            val credentials = LoginCredentials(email, "invalidpassword")
            post("/api/v1/login", credentials)
                .statusCode(401)
                .header("Authorization", nullValue())
                .contentType("application/json")
                .body("status", equalTo(401))
                .body("message", equalTo("invalid password"))
                .body("uri", equalTo("/api/v1/login"))
        }

        test("Invalid json should render 400") {
            post("/api/v1/login", "{{{{{{{{}")
                .statusCode(400)
                .header("Authorization", nullValue())
                .contentType("application/json")
                .body("status", equalTo(400))
                .body("message", containsString("Failed to parse request body"))
                .body("uri", equalTo("/api/v1/login"))
        }

        test("Missing fields json should render 404") {
            post("/api/v1/login", """{"email": "Sjakie", "key":"unknown-property"}""")
                .log().all()
                .statusCode(400)
                .header("Authorization", nullValue())
                .contentType("application/json")
                .body("status", equalTo(400))
                .body("message", containsString("missing property password"))
                .body("uri", equalTo("/api/v1/login"))
        }
    }

    private fun validateJwt(token: String, email: String) {
        val user = ApplicationTest.server?.auth?.extractUser(token)!!
        assertThat(user.name, equalTo(email))
    }
}