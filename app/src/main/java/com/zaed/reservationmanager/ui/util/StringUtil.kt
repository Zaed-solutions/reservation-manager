package com.zaed.reservationmanager.ui.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

fun Double.formatMoney(padding: Int = 0): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
        minimumFractionDigits = padding
        maximumFractionDigits = padding
    }
    val formattedAmount = formatter.format(this)
    return formattedAmount
}