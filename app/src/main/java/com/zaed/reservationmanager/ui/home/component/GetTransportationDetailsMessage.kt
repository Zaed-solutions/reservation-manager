package com.zaed.reservationmanager.ui.home.component

import android.content.Context
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.ui.util.formatEpochSecondsToMessageDateTime
import java.text.NumberFormat
import java.util.Locale

fun getTransportationDetailsMessage(
    context: Context,
    reservation: Reservation,
): String {
    val difference = reservation.travelRidePrice - reservation.travelCollectedAmount

    val additionalMessageResId = if (difference > 0) {
        R.string.positive_trip_balance
    } else {
        R.string.negative_trip_balance
    }

    val additionalMessage = context.getString(additionalMessageResId, kotlin.math.abs(difference))
    return context.getString(
        R.string.transportation_details,
        reservation.clientName,
        reservation.clientPhone,
        (reservation.date + reservation.time).formatEpochSecondsToMessageDateTime(),
        reservation.car,
        reservation.carCount, // New field
        reservation.startLocation,
        reservation.flightNumber,
        reservation.endLocation,
        context.getString(R.string.sar, NumberFormat.getInstance(Locale.getDefault()).format(reservation.travelRidePrice)),
        context.getString(R.string.sar, NumberFormat.getInstance(Locale.getDefault()).format(reservation.travelCollectedAmount)),
        reservation.note,
        additionalMessage // %12$s
    )
}