package nl.toefel.garsson.server.middleware

import io.undertow.util.AttachmentKey
import nl.toefel.garsson.auth.User

object Keys {
    val USER_ATTACHMENT: AttachmentKey<User> = AttachmentKey.create(User::class.java)
}