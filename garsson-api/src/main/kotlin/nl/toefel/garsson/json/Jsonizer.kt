package nl.toefel.garsson.json

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule

object Jsonizer {
    val mapper: ObjectMapper = jacksonObjectMapper()
        .registerModule(ParameterNamesModule())
        .registerModule(Jdk8Module())
        .registerModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)

    fun toJson(obj: Any?, format: Boolean = false): String =
        if (format)
            mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj)
        else
            mapper.writeValueAsString(obj)

    inline fun <reified T> fromJson(json: String): T = mapper.readValue(json)
    inline fun <reified T> fromJson(json: ByteArray): T = mapper.readValue(json)
}