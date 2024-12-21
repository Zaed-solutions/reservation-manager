package com.zaed.reservationmanager.ui.reservation.create.component

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.toSeconds() = this / 1000


fun convertSecondsToString(seconds: Long): String {
    val formatter = SimpleDateFormat("d MMM, yyyy", Locale.getDefault())
    return if(seconds != 0L) formatter.format(Date(seconds*1000L)) else ""
}
