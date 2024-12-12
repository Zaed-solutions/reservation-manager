package com.zaed.reservationmanager.ui.reservation.details.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Whatsapp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme
import com.zaed.reservationmanager.ui.util.formatEpochSecondsToDateTime

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReservationDetailsHeader(
    modifier: Modifier = Modifier,
    reservation: Reservation = Reservation(),
    onClientClicked: () -> Unit = {},
    onCopyPhone: (String) -> Unit = {},
    onMessagePhone: (String) -> Unit = {},
    onTourismCompanyClicked: () -> Unit = {},
    onTourismEmployeeClicked: () -> Unit = {},
    onSendConfirmationMessage: () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
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
                text = reservation.date.formatEpochSecondsToDateTime(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row (
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = reservation.clientName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onClientClicked() }
            )
            Row (
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Whatsapp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = reservation.clientPhone,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .combinedClickable(
                            onClick = {
                                onMessagePhone(reservation.clientPhone)
                            },
                            onLongClick = {
                                onCopyPhone(reservation.clientPhone)
                            }
                        )
                )

            }

        }
        DetailRow(
            modifier = Modifier.padding(top = 8.dp),
            label = stringResource(R.string.residence),
            value = reservation.clientCountry
        )
        DetailRow(
            label = stringResource(R.string.tourism_company),
            value = reservation.tourismCompany,
            onClick = {
                onTourismCompanyClicked()
            }
        )
        DetailRow(
            label = stringResource(id = R.string.phone_number),
            value = reservation.tourismCompanyPhone,
            onClick = {
                onMessagePhone(reservation.tourismCompanyPhone)
            },
            onLongClick = {
                onCopyPhone(reservation.tourismCompanyPhone)
            }
        )
        DetailRow(
            label = stringResource(R.string.tourism_employee),
            value = reservation.tourismEmployee,
            onClick = {
                onTourismEmployeeClicked()
            }
        )
        DetailRow(
            label = stringResource(id = R.string.phone_number),
            value = reservation.tourismEmployeePhone,
            onClick = {
                onMessagePhone(reservation.tourismCompanyPhone)
            },
            onLongClick = {
                onCopyPhone(reservation.tourismCompanyPhone)
            }
        )
        Button(
            enabled = !reservation.sentConfirmToCustomer,
            onClick = { onSendConfirmationMessage() },
            contentPadding = PaddingValues(vertical = 0.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Icon(
                imageVector = if(reservation.sentConfirmToCustomer) Icons.Default.Check else Icons.Default.Whatsapp,
                contentDescription = null,
                tint = if(reservation.sentConfirmToCustomer) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
            )
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = stringResource(if(reservation.sentConfirmToCustomer) R.string.confirmation_sent else R.string.send_confirmation_to_client)
            )
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun Preview() {
    ReservationManagerTheme {
        val reservation = Reservation(
            id = "123",
            flightNumber = "FLI5555",
            clientName = "Armand Berg",
            clientPhone = "(752) 266-3027",
            date = 7018,
            clientCountry = "Macau",
            tourismCompany = "expetenda",
            tourismCompanyPhone = "(826) 553-4425",
            tourismEmployee = "John Doe",
            tourismEmployeePhone = "(485) 748-9831",
            sentConfirmToCustomer = true
        )
        ReservationDetailsHeader(
            modifier = Modifier.padding(16.dp),
            reservation = reservation
        )
    }
}