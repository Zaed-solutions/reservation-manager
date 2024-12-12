package com.zaed.reservationmanager.ui.reservation.details.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    onClick: (() -> Unit) = {},
    onLongClick: (() -> Unit) = {}

) {
    AnimatedVisibility(value.isNotBlank()) {

        Row(
            modifier = modifier.fillMaxWidth()
        ) {
            Text(
                text = "$label:",
                style = style,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
            Text(
                text = value,
                style = style,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .combinedClickable(
                        onClick = { onClick() },
                        onLongClick = { onLongClick() }
                    )
            )
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun Preview() {
    ReservationManagerTheme {
        DetailRow(modifier = Modifier.padding(16.dp), label = "Phone Number", value = "+1234567890")
    }
}