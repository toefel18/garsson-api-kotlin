package nl.toefel.garsson.server.handlers

import io.undertow.server.HttpServerExchange
import io.undertow.util.HttpString
import nl.toefel.garsson.auth.JwtHmacAuthenticator
import nl.toefel.garsson.dto.ApiError
import nl.toefel.garsson.dto.LoginCredentials
import nl.toefel.garsson.dto.SuccessfulLoginResponse
import nl.toefel.garsson.dto.User
import nl.toefel.garsson.repository.UserEntity
import nl.toefel.garsson.repository.UsersTable
import nl.toefel.garsson.server.HandlerFun
import nl.toefel.garsson.server.Status
import nl.toefel.garsson.server.readRequestBody
import nl.toefel.garsson.server.sendJsonResponse
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

/**
 * Login handler (curried function)
 */
fun login(auth: JwtHmacAuthenticator): HandlerFun = { exchange: HttpServerExchange ->
    val credentials: LoginCredentials = exchange.readRequestBody()

    transaction {
        val user = UserEntity.find { UsersTable.email eq credentials.email }.firstOrNull()
        when {
            user == null -> exchange.sendJsonResponse(Status.UNAUTHORIZED, ApiError("user not found"))
            user.password == credentials.password -> {
                user.lastLoginTime = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS).toString()
                val jwt = auth.generateJwt(User(credentials.email, roles = user.roles.split(",").toList()))
                exchange.responseHeaders.put(HttpString("Authorization"), "Bearer $jwt")
                exchange.sendJsonResponse(Status.OK, SuccessfulLoginResponse(jwt))
            }
            else -> exchange.sendJsonResponse(Status.UNAUTHORIZED, ApiError("invalid password"))
        }
    }
}