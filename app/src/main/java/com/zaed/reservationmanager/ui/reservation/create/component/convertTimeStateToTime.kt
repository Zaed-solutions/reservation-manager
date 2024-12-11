package com.zaed.reservationmanager.ui.reservation.create.component

import android.util.Log
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
fun convertTimeStateToTime(selectedTime: TimePickerState): String {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY,selectedTime.hour)
    cal.set(Calendar.MINUTE, selectedTime.minute)
    cal.isLenient = false
    val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return formatter.format(cal.time)
}

@OptIn(ExperimentalMaterial3Api::class)
fun TimePickerState.toMillis(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, hour)
    cal.set(Calendar.MINUTE, minute)
    cal.isLenient = false
    Log.d("datessss", cal.timeInMillis.toString())
    return hour * 3600000L + minute * 60000L
//    return cal.timeInMillis
}