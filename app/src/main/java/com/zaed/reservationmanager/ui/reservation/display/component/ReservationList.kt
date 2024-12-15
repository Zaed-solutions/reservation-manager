package com.zaed.reservationmanager.ui.reservation.display.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.data.model.Reservation

@Composable
fun ReservationList(
    modifier: Modifier = Modifier,
    reservations: List<Reservation>,
    onNavigateToReservationDetails: (String) -> Unit = {},
    onDeleteReservation: (String) -> Unit = {},
    onNavigateToEditReservation: (Reservation) -> Unit = {}
) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 16.dp),
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(reservations) { reservation ->
            ExpandableReservationCard(
                reservation = reservation,
                onDeleteClicked = { onDeleteReservation(reservation.id) },
                onNavigateToEditReservation = onNavigateToEditReservation,
                onNavigateToReservationDetails = onNavigateToReservationDetails,

                )
        }
    }
}