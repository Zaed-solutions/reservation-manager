package com.zaed.reservationmanager.ui.reservation.create.component

import android.annotation.SuppressLint
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
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


@SuppressLint("SimpleDateFormat")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerFieldToModal(
    modifier: Modifier = Modifier,
    initialValue: Long = 0L,
    onTimeSelected: (Long?) -> Unit = {},
    errorMessage: ReservationError
) {
    var showModal by remember { mutableStateOf(false) }
    val initialMinutes = initialValue / 60L
    val timePickerState = rememberTimePickerState(
        initialHour = (initialMinutes / 60).toInt(),
        initialMinute = (initialMinutes%60).toInt(),
        is24Hour = false,
    )
    OutlinedTextField(
        value = convertTimeStateToTime(timePickerState),
        onValueChange = { },
        label = { Text(stringResource(R.string.time)) },
        placeholder = { Text("HH / MM / AM_PM") },
        trailingIcon = {
            Icon(Icons.Default.AccessTime, contentDescription = "Select Time")
        },
        isError = errorMessage == ReservationError.TIME_IS_REQUIRED,
        supportingText = {
            if (errorMessage == ReservationError.TIME_IS_REQUIRED) {
                Text(stringResource(errorMessage.messageRes))
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(timePickerState) {
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
        DialExample(
            onDismiss = {
                showModal = false
            },
            timePickerState = timePickerState,
            onConfirm = {
                onTimeSelected(timePickerState.toSeconds())
                showModal = false
            }
        )
    }
}