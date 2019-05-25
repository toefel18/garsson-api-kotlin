package nl.toefel.garsson

import io.restassured.RestAssured

/**
 * Adds methods for each HTTP verb that will execute a call to the server instance started by [ApplicationTest].
 *
 * Make sure ApplicationTest is part of your test as a TestListener:
 *
 * ```
 * class ExampleIntegrationTest : ApplicationRestTest, FunSpec() {
 *    override fun listeners(): List<TestListener> = listOf(ApplicationTest)
 *    init {
 *       test("My test") {
 *          get("/blaat").statusCode(200)
 *       }
 *    }
 * }
 * ```
 *
 * Most methods return a RestAssured ValidatableResponse which can be used to assert the response.
 */
interface ApplicationRestTest {

    fun get(uri: String) = RestAssured.given().log().all()
        .get("http://localhost:${ApplicationTest.config.port}$uri")
        .then().log().all()

    fun post(uri: String, data:Any) = RestAssured.given().log().all()
        .contentType("application/json")
        .body(data)
        .post("http://localhost:${ApplicationTest.config.port}$uri").then().log().all()
}