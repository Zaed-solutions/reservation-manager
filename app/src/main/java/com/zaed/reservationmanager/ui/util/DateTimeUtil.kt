package com.zaed.reservationmanager.ui.util

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

fun Long.formatEpochSecondsToDate(): String {
    val dateTime = java.time.Instant.ofEpochSecond(this).atZone(ZoneId.systemDefault()).toLocalDateTime()
    val formatter = DateTimeFormatter.ofPattern("d MMM, yyyy", Locale.getDefault())
    return dateTime.format(formatter)
}
fun Long.formatEpochSecondsToDateTime(): String {
    val dateTime = java.time.Instant.ofEpochSecond(this).atZone(ZoneId.systemDefault()).toLocalDateTime()
    val formatter = DateTimeFormatter.ofPattern("d MMM, yyyy, hh:mm a", Locale.getDefault())
    return dateTime.format(formatter)
}
fun getStartAndEndOfDay(epochSecond: Long, zoneId: ZoneId = ZoneId.systemDefault()): Pair<Long, Long> {
    val instant = java.time.Instant.ofEpochSecond(epochSecond)
    val localDateTime = instant.atZone(zoneId).toLocalDate().atStartOfDay(zoneId)

    // Start of the day in epoch seconds
    val startOfDay = localDateTime.toEpochSecond()

    // End of the day in epoch seconds
    val endOfDay = localDateTime.plus(1, ChronoUnit.DAYS).minusSeconds(1).toEpochSecond()

    return startOfDay to endOfDay
}