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


@SuppressLint("SimpleDateFormat")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerFieldToModal(
    modifier: Modifier = Modifier,
    onTimeSelected: (Long?) -> Unit = {}
) {
    var selectedTime: TimePickerState? by remember { mutableStateOf(null) }
    var showModal by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = selectedTime?.let { convertTimeStateToTime(it) } ?: "",
        onValueChange = { },
        label = { Text(stringResource(R.string.time)) },
        placeholder = { Text("HH / MM / AM_PM") },
        trailingIcon = {
            Icon(Icons.Default.AccessTime, contentDescription = "Select Time")
        },
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(selectedTime) {
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
            onConfirm = {
                selectedTime = it
                onTimeSelected(it.toMillis())
                showModal = false
            }
        )
    }
}