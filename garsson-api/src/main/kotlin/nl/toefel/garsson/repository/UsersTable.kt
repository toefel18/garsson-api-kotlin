package nl.toefel.garsson.repository

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.Column

/**
 * @see https://medium.com/@OhadShai/bits-and-blobs-of-kotlin-exposed-jdbc-framework-f1ee56dc8840
 * @See https://github.com/JetBrains/Exposed/wiki/DSL
 */

// plural because user is a reserved keyword in sql
object UsersTable : LongIdTable(columnName = "id") {
    val email: Column<String> = varchar("email", 255).uniqueIndex()
    val password: Column<String> = varchar("password", 255)
    val roles: Column<String> = varchar("roles", 512)
    val createdTime: Column<String> = varchar("created_time", 64)
    val lastEditTime: Column<String> = varchar("last_edit_time", 64)
    val lastLoginTime: Column<String?> = varchar("last_login_time", 64).nullable()
}

class UserEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<UserEntity>(UsersTable)

    var email by UsersTable.email
    var password by UsersTable.password
    var roles by UsersTable.roles
    var createdTime by UsersTable.createdTime
    var lastEditTime by UsersTable.lastEditTime
    var lastLoginTime by UsersTable.lastLoginTime
}
