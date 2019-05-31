package nl.toefel.garsson.util

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.time.Clock
import java.time.Instant
import java.time.ZoneId


class UtilTest : StringSpec({

    "now() should return current date-time in UTC" {
        val clock = Clock.fixed(Instant.ofEpochMilli(1559189744730), ZoneId.of("Europe/Amsterdam"))
        now(clock) shouldBe "2019-05-30T04:15:44Z"
    }

})