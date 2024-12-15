package com.zaed.reservationmanager.ui.util

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

fun Long.formatEpochSecondsToDate(): String {
    val dateTime = Instant.fromEpochSeconds(this).toLocalDateTime(TimeZone.currentSystemDefault())
    val month = dateTime.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
    val day = dateTime.dayOfMonth
    val year = dateTime.year
    return "$month $day, $year"
}
fun Long.formatEpochSecondsToDateTime(): String {
    val dateTime = Instant.fromEpochSeconds(this).toLocalDateTime(TimeZone.currentSystemDefault())
    val month = dateTime.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
    val day = dateTime.dayOfMonth
    val year = dateTime.year
    val hour = dateTime.hour % 12
    val formattedHour = (if (hour == 0) 12 else hour).toString().padStart(2, '0')
    val minute = dateTime.minute.toString().padStart(2, '0')
    val amPm = if (dateTime.hour < 12) "am" else "pm"

    return "$month $day, $year, $formattedHour:$minute $amPm"
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