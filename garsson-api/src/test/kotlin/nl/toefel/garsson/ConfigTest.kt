package nl.toefel.garsson

import io.kotlintest.matchers.match
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

internal class ConfigTest : StringSpec({

    "toIntOrDefault should not return default on message" {
        "123".toIntOrDefault(8) shouldBe 123
        "".toIntOrDefault(8) shouldBe 8
        "-".toIntOrDefault(8) shouldBe 8
    }

    "Config.fromEnvironment" {
        val cfg = Config.fromEnvironment()
        cfg.port shouldBe 8080
        cfg.jwtSigningSecret shouldBe "jwt_secret_for_test"
    }

    "safeForLogging should gray out secrets" {
        val cfg = Config.fromEnvironment()
        cfg.safeForLogging().jwtSigningSecret should match("""^\*+$""")
    }
})

