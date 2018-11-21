package nl.toefel.garsson

import io.kotlintest.shouldBe
import io.kotlintest.shouldEqual
import io.kotlintest.matchers.match
import io.kotlintest.should
import io.kotlintest.specs.StringSpec

internal class ConfigTest :StringSpec({

    "toIntOrDefault should not return default on error" {
        "123".toIntOrDefault(8) shouldEqual 123
        "".toIntOrDefault(8) shouldEqual 8
        "-".toIntOrDefault(8) shouldEqual 8
    }

    "Config.fromEnvironment" {
        val cfg = Config.fromEnvironment()
        cfg.port shouldEqual 8080
        cfg.jwtSigningSecret shouldEqual "jwt_secret_for_test"
    }

    "safeForLogging should gray out secrets" {
        val cfg = Config.fromEnvironment()
        cfg.safeForLogging().jwtSigningSecret should match("""^\*+$""")
    }
})

