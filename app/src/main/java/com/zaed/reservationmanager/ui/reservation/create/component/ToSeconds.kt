package com.zaed.reservationmanager.ui.reservation.create.component

import com.zaed.reservationmanager.ui.home.component.ReportLanguage
import com.zaed.reservationmanager.ui.util.formatEpochSecondsToMonthlyDate
import org.bouncycastle.oer.its.ieee1609dot2.basetypes.Duration.seconds
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DecimalStyle
import java.util.Date
import java.util.Locale

fun Long.toSeconds() = this / 1000


fun convertSecondsToString(seconds: Long): String {
    val formatter = SimpleDateFormat("d MMM, yyyy", Locale.getDefault())
    return if(seconds != 0L) formatter.format(Date(seconds*1000L)) else ""
}

fun convertRangeToString( range: Pair<Long?, Long?>,language: ReportLanguage=ReportLanguage.Arabic,): String {
    val fromDateTime = java.time.Instant.ofEpochSecond((range.first?:0)).atZone(ZoneId.of("UTC")).toLocalDateTime()
    val toDateTime = java.time.Instant.ofEpochSecond((range.second?:0)).atZone(ZoneId.of("UTC")).toLocalDateTime()
    val formatter = DateTimeFormatter.ofPattern("dd/MMMM", if(language == ReportLanguage.Arabic) Locale("ar") else Locale("en")).withDecimalStyle(
        DecimalStyle.of(if(language == ReportLanguage.Arabic) Locale("ar") else Locale("en"))
    )
    return (if(range.first == 0L) "" else  formatter.format(fromDateTime))+ " - " + (if(range.second == 0L) "" else formatter.format(toDateTime))
}
