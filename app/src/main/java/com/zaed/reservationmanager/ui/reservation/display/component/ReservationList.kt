package com.zaed.reservationmanager.ui.reservation.display.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Reservation

@Composable
fun ReservationList(
    modifier: Modifier = Modifier,
    reservations: List<Reservation>,
    onNavigateToReservationDetails: (String) -> Unit = {},
    onDeleteReservation: (String) -> Unit = {},
    onNavigateToEditReservation: (Reservation) -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedContent(targetState = reservations.isEmpty()) { state ->
            when {
                state -> {
                    Text(
                        modifier = Modifier.padding(top = 36.dp),
                        text = stringResource(R.string.no_reservations_added),
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                else -> {
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
            }
        }
    }
}