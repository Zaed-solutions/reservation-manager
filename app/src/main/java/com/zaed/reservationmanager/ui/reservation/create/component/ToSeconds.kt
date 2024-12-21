package com.zaed.reservationmanager.ui.reservation.create.component

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.toSeconds() = this / 1000


fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    return if(millis != 0L) formatter.format(Date(millis)) else ""
}
