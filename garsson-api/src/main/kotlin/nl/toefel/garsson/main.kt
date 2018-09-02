package nl.toefel.garsson

import io.undertow.Undertow
import java.nio.charset.Charset

fun main(args: Array<String>) {
    val undertow = Undertow.builder()
            .addHttpListener(8080, "0.0.0.0")
            .setHandler { exchange -> exchange.responseSender.send("blaat", Charset.defaultCharset()) }
            .build()

    undertow.start()
}