package nl.toefel.garsson.dto

import nl.toefel.garsson.util.now

data class ApiError(
    val message: String,

    val uri: String = "",
    val status: Int = -1,
    val serverTime: String = now()
)