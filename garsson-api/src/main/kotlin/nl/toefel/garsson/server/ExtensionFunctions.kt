package nl.toefel.garsson.server

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.server.handlers.BlockingHandler
import io.undertow.util.Headers
import nl.toefel.garsson.dto.ApiError
import nl.toefel.garsson.json.Jsonizer
import nl.toefel.garsson.server.middleware.Attachments
import nl.toefel.garsson.server.middleware.BasicErrorHandler
import nl.toefel.garsson.server.middleware.RequireRoleHandler

/** Unmarshals the request body to type [T] and attaches the the String representation to the exchange for logging by [RequestLoggingHandler] */
inline fun <reified T> HttpServerExchange.readRequestBody(logOnError: Boolean = true): T {
    val requestBody = this.inputStream.readAllBytes()
    try {
        if (logOnError) {
            this.putAttachment(Attachments.REQUEST_BODY, String(requestBody))
        }
        // TODO add parsing based on Content-Type header, for now, assume JSON
        return Jsonizer.fromJson(requestBody)
    } catch (ex: JsonParseException) {
        throw BodyParseException("Failed to parse request body to ${T::class.java.simpleName}", requestBody, ex)
    } catch (ex: MissingKotlinParameterException) {
        throw BodyParseException("Failed to parse request body to ${T::class.java.simpleName}, missing property ${ex.parameter.name}", requestBody, ex)
    }
}

/**
 * Sends data marshaled as JSON and puts the String representation as an attachment to the exchange for logging by [RequestLoggingHandler].
 *
 * When data is [ApiError], status and uri will be overwritten with actual values.
 */
fun HttpServerExchange.sendJsonResponse(code: Int, data: Any) {
    this.statusCode = code
    this.responseHeaders.put(Headers.CONTENT_TYPE, MediaType.APPLICATION_JSON)
    val body = if (data is ApiError) {
        data.copy(
            status = this.statusCode,
            uri = if (data.uri == "") this.requestURI else data.uri)
    } else {
        data
    }
    val response = Jsonizer.toJson(body)
    this.putAttachment(Attachments.RESPONSE_BODY, response)
    this.responseSender.send(response)
}

fun HttpServerExchange.requireParam(name: String): String =
    this.queryParameters[name]?.first ?: throw MissingRequiredParameter(name)

fun HttpServerExchange.requireParamAsLong(name: String): Long {
    val param = requireParam(name)
    return param.toLongOrNull() ?: throw InvalidParameterFormat(name, "Long", param)
}

/** Alias for a handler that is a function reference */
typealias HandlerFun = (HttpServerExchange) -> Unit

/** Adds a role requirement to a HandlerFun */
infix fun HandlerFun.requiresRole(role: String) = RequireRoleHandler(listOf(role), this)

/** Adds a role requirement to a HttpHandler */
infix fun HttpHandler.requiresRole(role: String) = RequireRoleHandler(listOf(role), this)

/** Wraps the handler in a BlockingHandler (a blocking handler dispatches the request to a worker thread) */
val HttpHandler.blocks get() = BlockingHandler(this)

/** Wraps the handler in a BlockingHandler (a blocking handler dispatches the request to a worker thread) */
val HandlerFun.blocks get() = BlockingHandler(this)


/** Wraps the handler in a BlockingHandler (a blocking handler dispatches the request to a worker thread) */
val HttpHandler.basicErrors get() = BasicErrorHandler(this)

/** Wraps the handler in a BlockingHandler (a blocking handler dispatches the request to a worker thread) */
val HandlerFun.basicErrors get() = BasicErrorHandler(this)
