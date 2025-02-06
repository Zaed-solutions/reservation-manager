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
        reservation.clientName.trim(),
        reservation.clientPhone.trim(),
        (reservation.date + reservation.time).formatEpochSecondsToMessageDateTime(),
        reservation.car.trim(),
        reservation.carCount,
        reservation.startLocation.trim(),
        reservation.endLocation.trim(),
        context.getString(R.string.sar, NumberFormat.getInstance(Locale.getDefault()).format(reservation.travelRidePrice)),
        context.getString(R.string.sar, NumberFormat.getInstance(Locale.getDefault()).format(reservation.travelCollectedAmount)),
        if(reservation.note.isBlank())"" else context.getString(R.string.note_temp, reservation.note.trim()),
        additionalMessage
    )
}
fun getClientConfirmationMessage(context: Context, reservation: Reservation): String{
    return context.getString(
        R.string.confirmation_message,
        reservation.clientName.trim(),
        (reservation.date + reservation.time).formatEpochSecondsToMessageDateTime(),
        "${reservation.carCount} ${reservation.car}",
        reservation.startLocation.trim(),
        reservation.endLocation.trim(),
        if(reservation.tourismCollectedAmount > 0) context.getString(R.string.sar, NumberFormat.getInstance(Locale.getDefault()).format(reservation.tourismCollectedAmount)) else context.getString(R.string.paid),
    )
}
fun getDriverInfoMessage(context: Context, reservation: Reservation): String{
    return context.getString(
        R.string.reservation_details_message,
        reservation.clientName.trim(),
        (reservation.date + reservation.time).formatEpochSecondsToMessageDateTime(),
        "${reservation.carCount} ${reservation.car}",
        reservation.driver.trim(),
        reservation.driverPhoneNumber.trim()
    )
}
fun getThanksMessage(context: Context, reservation: Reservation): String{
    return context.getString(
        R.string.thanks_message,
        reservation.clientName.trim()
    )
}