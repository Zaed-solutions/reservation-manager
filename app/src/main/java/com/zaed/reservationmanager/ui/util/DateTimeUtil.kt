package com.zaed.reservationmanager.ui.util

import com.zaed.reservationmanager.ui.home.component.TimeFilter
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DecimalStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

fun Long.formatEpochSecondsToDate(): String {
    val dateTime =
        java.time.Instant.ofEpochSecond(this).atZone(ZoneId.of("UTC")).toLocalDateTime()
    val formatter = DateTimeFormatter.ofPattern("d MMM, yyyy", Locale.getDefault()).withDecimalStyle(
        DecimalStyle.of(Locale.getDefault())
    )
    return dateTime.format(formatter)
}
fun TimeFilter.getDate(): String {
    val dateTime =
        when(this){
            TimeFilter.Today -> java.time.Instant.now().atZone(ZoneId.of("UTC")).toLocalDateTime()
            TimeFilter.Yesterday -> java.time.Instant.now().minus(1, ChronoUnit.DAYS).atZone(ZoneId.of("UTC")).toLocalDateTime()
            TimeFilter.Tomorrow -> java.time.Instant.now().plus(1, ChronoUnit.DAYS).atZone(ZoneId.of("UTC")).toLocalDateTime()
            is TimeFilter.FixedDate -> java.time.Instant.ofEpochSecond(this.date).atZone(ZoneId.of("UTC")).toLocalDateTime()
            else -> return ""
        }
    val locale = Locale.getDefault()
    val isArabic = locale.language == "ar"
    val formatterPattern = if (isArabic) "EEEE dd/MMMM/yyyy'\u0645'" else "EEEE dd/MMMM/yyyy"
    val formatter = DateTimeFormatter.ofPattern(formatterPattern, locale).withDecimalStyle(
        DecimalStyle.of(Locale.getDefault())
    )
    return dateTime.format(formatter)
}

fun Long.formatEpochSecondsToMonthlyDate(): String {
    val dateTime =
        java.time.Instant.ofEpochSecond(this).atZone(ZoneId.of("UTC")).toLocalDateTime()
    val formatter = DateTimeFormatter.ofPattern("dd/MMMM", Locale.getDefault()).withDecimalStyle(
        DecimalStyle.of(Locale.getDefault())
    )
    return dateTime.format(formatter)
}

fun Long.formatEpochSecondsToDateTime(): String {
    val dateTime =
        java.time.Instant.ofEpochSecond(this).atZone(ZoneId.of("UTC")).toLocalDateTime()
    val formatter = DateTimeFormatter.ofPattern("d MMM, yyyy, hh:mm a", Locale.getDefault()).withDecimalStyle(
        DecimalStyle.of(Locale.getDefault())
    )
    return dateTime.format(formatter)
}
fun Long.formatEpochSecondsToMessageDateTime(): String {
    val dateTime =
        java.time.Instant.ofEpochSecond(this).atZone(ZoneId.of("UTC")).toLocalDateTime()
    val formatter = DateTimeFormatter.ofPattern("d MMM, hh:mm a", Locale.getDefault()).withDecimalStyle(
        DecimalStyle.of(Locale.getDefault())
    )
    return "*${dateTime.format(formatter)}*"
}
