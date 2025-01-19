package com.zaed.reservationmanager.ui.home.component

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ReservationsList(
    modifier: Modifier = Modifier,
    reservations: List<Reservation> = emptyList(),
    onAddReservation: () -> Unit = {},
    onArchiveReservation: (String) -> Unit = {},
    isHeaderVisible: Boolean = true,
    isAddEnabled: Boolean = true,
    isSendActionsVisible: Boolean = true,
    isEditable: Boolean = true,
    onDeleteReservation: (reservation: Reservation) -> Unit = {},
    onCopyPhoneNumber: (String) -> Unit = {},
    onMessagePhoneNumber: (String) -> Unit = {},
    onEditProfile: (String) -> Unit = {},
    isEditProfileEnabled: Boolean,
    onEditReservation: (reservation: Reservation) -> Unit = {},
    onSendConfirmationToCustomer: (reservationId: String) -> Unit = {},
    onSendDriverInfoToClient: (reservationId: String) -> Unit = { },
    onSendInfoToTravelCompany: (reservationId: String) -> Unit = {},
    onSendThanksMessageToCustomer: (reservationId: String) -> Unit = {},
    scope: CoroutineScope = rememberCoroutineScope(),
    context: Context = LocalContext.current,
    onAddSecondaryReservation: (mainReservation: Reservation) -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isHeaderVisible) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.add_reservation),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                if (isAddEnabled) {
                    IconButton(onClick = { onAddReservation() }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Reservation"
                        )
                    }
                }
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
                    val lazyState = rememberLazyListState()
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        state = lazyState,
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(reservations) { reservation ->
                            ReservationItem(
                                modifier = Modifier.animateItem(),
                                reservation = reservation,
                                isActionsVisible = isSendActionsVisible,
                                onDeleteReservation = {
                                    onDeleteReservation(reservation)
                                },
                                isEditable = isEditable,
                                onEditReservation = {
                                    onEditReservation(reservation)
                                },
                                onArchiveReservation = {
                                    onArchiveReservation(reservation.id)
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
                                },
                                onSendThanksToCustomer = {
                                    onSendThanksMessageToCustomer(reservation.id)
                                },
                                isEditProfileEnabled = isEditProfileEnabled,
                                onEditProfile = {
                                    onEditProfile(reservation.clientId)
                                },
                                onAddSecondaryReservation = {
                                    onAddSecondaryReservation(reservation)
                                },
                                onViewMainReservation = {
                                    scope.launch {
                                        val index =reservations.indexOfFirst { it.mainReservation && it.reservationNumber == reservation.reservationNumber }
                                        if(index != -1) {
                                            lazyState.scrollToItem(index)
                                        } else {
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.main_reservation_not_in_this_list),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun Preview() {
    ReservationManagerTheme {
        val reservations = listOf(
            Reservation(
                id = "tristique",
                date = 7041,
                type = "Mazarat El Madina",
                car = "Camaro",
                travelCompanyPhone = "(398) 742-4872",
                driver = "Ahmed Mohsen",
                travelCompany = "Gawhara Travel Company",
                startLocation = "Gadda",
                endLocation = "Riyadh",
//                buyingPrice = 1,
//                sellingPrice = 2,
//                collectedAmount = 4,
                note = "unum unum unum unum unum unum unum unum unum unum unum unum unum unum unum unum unum ",
                sentDriverInfoToCustomer = false,
                sentToDriverCompany = true
            ),
            Reservation(
                id = "tristique",
                date = 7041,
                type = "Mazarat El Madina",
                car = "Camaro",
                travelCompanyPhone = "(398) 742-4872",
                driver = "Ahmed Mohsen",
                travelCompany = "Gawhara Travel Company",
                startLocation = "Gadda",
                endLocation = "Riyadh",
//                buyingPrice = 0,
//                sellingPrice = 2,
//                collectedAmount = 4,
                note = "unum unum unum unum unum unum unum unum unum unum unum unum unum unum unum unum unum ",
                sentDriverInfoToCustomer = false,
                sentToDriverCompany = true
            )
        )
        ReservationsList(
            modifier = Modifier.padding(16.dp),
            reservations = emptyList(),
            isEditProfileEnabled = false
        )
    }
}