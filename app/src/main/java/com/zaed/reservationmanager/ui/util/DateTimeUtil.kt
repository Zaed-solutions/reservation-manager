package com.zaed.reservationmanager.ui.util

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

fun Long.formatEpochSecondsToDate(): String {
    val dateTime =
        java.time.Instant.ofEpochSecond(this).atZone(ZoneId.of("UTC")).toLocalDateTime()
    val formatter = DateTimeFormatter.ofPattern("d MMM, yyyy", Locale.getDefault())
    return dateTime.format(formatter)
}

fun Long.formatEpochSecondsToDateTime(): String {
    val dateTime =
        java.time.Instant.ofEpochSecond(this).atZone(ZoneId.of("UTC")).toLocalDateTime()
    val formatter = DateTimeFormatter.ofPattern("d MMM, yyyy, hh:mm a", Locale.getDefault())
    return dateTime.format(formatter)
}
fun Long.formatEpochSecondsToMessageDateTime(): String {
    val dateTime =
        java.time.Instant.ofEpochSecond(this).atZone(ZoneId.of("UTC")).toLocalDateTime()
    val formatter = DateTimeFormatter.ofPattern("d MMM, hh:mm a", Locale.getDefault())
    return dateTime.format(formatter)
}
