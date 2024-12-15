package com.zaed.reservationmanager.ui.reservation.create.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.zaed.reservationmanager.R
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialExample(
    onConfirm: (TimePickerState) -> Unit,
    onDismiss: () -> Unit,
) {
    val currentTime = Calendar.getInstance()

    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = false,
    )
    Surface(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
    ) {
        Dialog (
            onDismissRequest = onDismiss,
            content = {
                Card() {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TimePicker(
                            state = timePickerState,
                        )
                        Row {
                            Button(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(4.dp),
                                shape = RoundedCornerShape(12.dp),
                                onClick = onDismiss
                            ) {
                                Text(stringResource(R.string.dismiss))
                            }
                            Button(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(4.dp),
                                shape = RoundedCornerShape(12.dp),
                                onClick = {
                                    onConfirm(timePickerState)
                                    onDismiss()
                                }
                            ) {
                                Text(stringResource(R.string.confirm))
                            }
                        }

                    }
                }})

    }
}