package nl.toefel.garsson.server.handlers

import io.undertow.server.HttpServerExchange
import io.undertow.util.Headers
import io.undertow.util.StatusCodes
import nl.toefel.garsson.json.Jsonizer
import nl.toefel.garsson.server.HandlerFun
import nl.toefel.garsson.server.sendJsonResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers

fun triggerGoogleLogin(): HandlerFun = { exchange: HttpServerExchange ->
    val redirectLocation = "https://accounts.google.com/o/oauth2/v2/auth?client_id=149947213830-o96fofmcmot8f1m2u5vthgtofti4gjh5.apps.googleusercontent.com&response_type=code&access_type=offline&redirect_uri=http://localhost:8080/api/v1/google/callback&scope=profile email"
    exchange.statusCode = StatusCodes.FOUND
    exchange.responseHeaders.put(Headers.LOCATION, redirectLocation)
    exchange.endExchange()
}

fun authorizationGrantCallback(): HandlerFun = { exchange: HttpServerExchange ->
    val code = exchange.queryParameters["code"]?.first
    val scopes = exchange.queryParameters["scope"]?.first?.split(" ")?.toList()

    logger.info("Received authorization grant: $code")
    logger.info("Received scopes: $scopes")

    val formData = mapOf(
        "code" to code,
        "client_id" to "149947213830-o96fofmcmot8f1m2u5vthgtofti4gjh5.apps.googleusercontent.com",
        "client_secret" to "<SECRET>",
        "redirect_uri" to "http://localhost:8080/api/v1/google/callback",
        "grant_type" to "authorization_code"
    )

    val client = HttpClient.newHttpClient()
    val request = HttpRequest.newBuilder()
        .POST(HttpRequest.BodyPublishers.ofString(Jsonizer.toJson(formData)))
        .uri(URI.create("https://oauth2.googleapis.com/token"))
        .build()

    val result = client.send(request, BodyHandlers.ofString())

    logger.info("Final result received ${result.statusCode()} ${result.body()}")
    exchange.sendJsonResponse(200, ReceivedGrant(code!!, scopes!!, result.body()))
}

data class ReceivedGrant(val grant:String,val scopes: List<String>, val finalResult :String)
val logger : Logger = LoggerFactory.getLogger("auth")