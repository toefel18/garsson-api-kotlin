package nl.toefel.garsson

import io.kotlintest.specs.StringSpec
import me.ntrrgc.tsGenerator.TypeScriptGenerator
import nl.toefel.garsson.dto.*
import java.time.LocalDate
import java.time.LocalDateTime


internal class GenerateTypings : StringSpec({

    "generate typings" {
        println()
        println()
        println(TypeScriptGenerator(
            rootClasses = setOf(
                Order::class,
                ApiError::class,
                LoginCredentials::class,
                SuccessfulLoginResponse::class,
                QuantityUnit::class,
                Version::class,
                User::class
            ),
            mappings = mapOf(
                LocalDateTime::class to "Date",
                LocalDate::class to "Date"
            )
        ).definitionsText)
    }

})
