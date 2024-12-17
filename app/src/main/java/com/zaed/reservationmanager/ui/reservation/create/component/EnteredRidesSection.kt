package com.zaed.reservationmanager.ui.reservation.create.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.ReservationModel
import com.zaed.reservationmanager.ui.reservation.details.components.ReservationItem

@Composable
fun EnteredRidesSection(
    isEditMode: Boolean,
    reservationModels: List<ReservationModel>,
    onAddMovementClicked: () -> Unit,
    onEditRide: (ReservationModel) -> Unit,
    onDeleteRide: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.rides),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onAddMovementClicked,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.add_ride)
            )
        }
    }
    reservationModels.forEach { ride ->
        ReservationItem(
            reservation = ride,
            isEditEnabled = isEditMode,
            onDeleteReservation = { onDeleteRide(ride.id) },
            onEditReservation = { onEditRide(ride) },
            isActionsVisible = false
        )
    }
}