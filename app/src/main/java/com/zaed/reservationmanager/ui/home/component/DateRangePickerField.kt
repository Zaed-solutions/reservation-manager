package com.zaed.reservationmanager.ui.home.component

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
import com.zaed.reservationmanager.ui.reservation.create.component.convertRangeToString


@Composable
fun DateRangePickerField(
    modifier: Modifier = Modifier,
    onDateRangeSelected: (Pair<Long?, Long?>) -> Unit = {},
) {
    var selectedDateRange by remember { mutableStateOf(Pair(0L, 0L)) }
    var showModal by remember { mutableStateOf(false) }

    OutlinedTextField(value = convertRangeToString(selectedDateRange),
        onValueChange = {},
        label = { Text(stringResource(R.string.date)) },
        placeholder = { Text(stringResource(R.string.dd_mm_yyyy)) },
        trailingIcon = {
            Icon(Icons.Default.DateRange, contentDescription = "Select date")
        },
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(selectedDateRange) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    if (upEvent != null) {
                        showModal = true
                    }
                }
            })

    if (showModal) {
        DateRangePickerModal(onDateRangeSelected = {
            selectedDateRange = Pair((it.first?:0)/1000L, (it.second ?:0) / 1000L)
            onDateRangeSelected(it)
            showModal = false
        }, onDismiss = { showModal = false })
    }
}