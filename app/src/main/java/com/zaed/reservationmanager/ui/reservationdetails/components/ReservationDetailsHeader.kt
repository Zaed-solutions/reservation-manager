package com.zaed.reservationmanager.ui.reservationdetails.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme

@Composable
fun ReservationDetailsHeader(
    modifier: Modifier = Modifier,
    clientName: String = "",
    onClientClicked: () -> Unit = {},
    customerPhone: String = "",
    tourismCompany: String = "",
    tourismCompanyPhone: String = "",
    tourismEmployee: String = "",
    tourismEmployeePhone: String = "",
    flightNumber: String = "",
    date: String = "",
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                contentDescription = "Reservation Date",
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = clientName,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .padding(top = 8.dp)
                .clickable { onClientClicked() }
        )
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun Preview() {
    ReservationManagerTheme {
        ReservationDetailsHeader(
            modifier = Modifier.padding(16.dp),
            clientName = "John Doe",
            date = "2022-01-01, 13:30"
        )
    }
}