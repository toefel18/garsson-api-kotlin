package nl.toefel.garsson.server

import io.kotlintest.extensions.TestListener
import io.kotlintest.specs.FunSpec
import nl.toefel.garsson.ApplicationRestTest
import nl.toefel.garsson.ApplicationTest
import org.hamcrest.CoreMatchers.*

class VersionIntegrationTest : ApplicationRestTest, FunSpec() {
    override fun listeners(): List<TestListener> = listOf(ApplicationTest)

    init {
        test("version should include server time") {
            get("/version")
                .statusCode(200)
                .body("application", equalTo("garsson-api"))
                .body("serverTime", `is`(notNullValue()))
        }
    }
}