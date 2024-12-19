package com.zaed.reservationmanager.ui.reservation.create.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.CompanyType
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.ui.home.component.ReservationItem

@Composable
fun AddedReservationsList(
    modifier: Modifier = Modifier,
    reservations: List<Reservation> = emptyList(),
    onAddReservation: () -> Unit = {},
    isSendActionsVisible: Boolean = true,
    isEditable: Boolean = true,
    onDeleteReservation: (reservationId: String) -> Unit = {},
    onCompanyClicked: (companyId: String, companyType: CompanyType) -> Unit = { _, _ -> },
    onCopyPhoneNumber: (String) -> Unit = {},
    onMessagePhoneNumber: (String) -> Unit = {},
    onEditReservation: (reservation: Reservation) -> Unit = {},
    onSendConfirmationToCustomer: (reservationId: String) -> Unit = {},
    onSendDriverInfoToClient: (reservationId: String) -> Unit = { },
    onSendInfoToTravelCompany: (reservationId: String) -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.reservations),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = { onAddReservation() }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Reservation"
                )
            }
        }

        AnimatedContent(targetState = reservations.isEmpty()) { state ->
            when {
                state -> {
                    Text(
                        modifier = Modifier.padding(top = 36.dp),
                        text = stringResource(R.string.no_reservations_added),
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                else -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        reservations.forEach { reservation ->
                            ReservationItem(
                                reservation = reservation,
                                isActionsVisible = isSendActionsVisible,
                                onDeleteReservation = {
                                    onDeleteReservation(reservation.id)
                                },
                                isEditable = isEditable,
                                onEditReservation = {
                                    onEditReservation(reservation)
                                },
                                onCompanyClicked = { companyId, companyType ->
                                    onCompanyClicked(companyId, companyType)
                                },
                                onCopyPhoneNumber = { number ->
                                    onCopyPhoneNumber(number)
                                },
                                onMessagePhoneNumber = { number ->
                                    onMessagePhoneNumber(number)
                                },
                                onSendDriverInfoToClient = {
                                    onSendDriverInfoToClient(
                                        reservation.id
                                    )
                                },
                                onSendInfoToTravelCompany = {
                                    onSendInfoToTravelCompany(reservation.id)
                                },
                                onSendConfirmationToCustomer = {
                                    onSendConfirmationToCustomer(reservation.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}