package nl.toefel.garsson.json

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

object Jsonizer {

    val mapper = jacksonObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

    fun toJson(obj: Any?, format: Boolean = false): String =
            if (format)
                mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj)
            else
                mapper.writeValueAsString(obj)

    inline fun <reified T> fromJson(json: String): T = mapper.readValue(json)
    inline fun <reified T> fromJson(json: ByteArray): T = mapper.readValue(json)
}