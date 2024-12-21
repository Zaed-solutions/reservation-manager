package com.zaed.reservationmanager.ui.reservation.create.component

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.ui.reservation.create.ReservationError

@Composable
fun DatePickerFieldToModal(
    modifier: Modifier = Modifier,
    initialValue: Long = 0L,
    onDateSelected: (Long?) -> Unit = {},
    errorMessage: ReservationError
) {
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var showModal by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = selectedDate?.let { convertMillisToDate(it) } ?: "",
        onValueChange = { },
        label = { Text(stringResource(R.string.date)) },
        placeholder = { Text(stringResource(R.string.dd_mm_yyyy)) },
        trailingIcon = {
            Icon(Icons.Default.DateRange, contentDescription = "Select date")
        },
        isError = errorMessage == ReservationError.DATE_IS_REQUIRED,
        supportingText = {
            if (errorMessage == ReservationError.DATE_IS_REQUIRED) {
                Text(stringResource(errorMessage.messageRes))
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(selectedDate) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    if (upEvent != null) {
                        showModal = true
                    }
                }
            }
    )

    if (showModal) {
        DatePickerModal(
            initialValue = initialValue,
            onDateSelected = {
                selectedDate = it
                onDateSelected(it?.toSeconds())
                showModal = false
            },
            onDismiss = { showModal = false }
        )
    }
}