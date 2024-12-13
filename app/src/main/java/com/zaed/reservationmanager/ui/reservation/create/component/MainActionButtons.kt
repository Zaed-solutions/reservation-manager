package com.zaed.reservationmanager.ui.reservation.create.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.ui.reservation.create.ReservationUiAction

@Composable
fun MainActionButtons(
    action: (ReservationUiAction) -> Unit,
    isEditMode: Boolean = false,
    onBackClicked: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Button(
            modifier = Modifier
                .weight(1f)
                .padding(4.dp),
            onClick = {action(ReservationUiAction.SaveReservation)},
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (isEditMode) stringResource(R.string.save_changes)
                    else stringResource(R.string.save_reservation)
            )
        }
        Button(
            modifier = Modifier
                .weight(1f)
                .padding(4.dp),
            shape = RoundedCornerShape(12.dp),
            onClick = {
                action(ReservationUiAction.Cancel)
                onBackClicked()
            }
        ) {
            Text(stringResource(R.string.cancel))
        }
    }

}