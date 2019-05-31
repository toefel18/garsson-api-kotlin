package nl.toefel.garsson.util

import java.time.Clock
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

fun now(clock: Clock = Clock.systemDefaultZone()): String = OffsetDateTime.now(clock)
    .withOffsetSameInstant(ZoneOffset.UTC)
    .truncatedTo(ChronoUnit.SECONDS)
    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

