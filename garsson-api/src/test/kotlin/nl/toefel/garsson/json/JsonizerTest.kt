package nl.toefel.garsson.json

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec


data class SomeDto(val a: String, val b: Int)

class JsonizerTest : StringSpec({

    "toJson without formatting" {
        Jsonizer.toJson(SomeDto("hello", 1234567)) shouldBe """{"a":"hello","b":1234567}"""
    }

    "toJson with formatting" {
        Jsonizer.toJson(SomeDto("hello", 1234567), format = true) shouldBe """{
            |  "a" : "hello",
            |  "b" : 1234567
            |}""".trimMargin()
    }

    "fromJson" {
        val data: SomeDto = Jsonizer.fromJson("""{"a":"hello","b":1234567}""")
        data.a shouldBe "hello"
        data.b shouldBe 1234567
    }
})