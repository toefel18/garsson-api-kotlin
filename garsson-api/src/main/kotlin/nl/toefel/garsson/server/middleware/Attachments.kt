package nl.toefel.garsson.server.middleware

import io.undertow.util.AttachmentKey
import nl.toefel.garsson.auth.User

object Attachments {
    val USER: AttachmentKey<User> = AttachmentKey.create(User::class.java)
    val REQUEST_BODY: AttachmentKey<String> = AttachmentKey.create(String::class.java)
    val RESPONSE_BODY: AttachmentKey<String> = AttachmentKey.create(String::class.java)
}
