package com.zaed.reservationmanager.ui.reservation.details.components

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Ride
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme

@Composable
fun RidesList(
    modifier: Modifier = Modifier,
    rides: List<Ride> = emptyList(),
    onAddRide: () -> Unit = {},
    onDeleteRide: (rideId: String) -> Unit = {},
    onCompanyClicked:(companyId: String) -> Unit = {},
    onDriverClicked: (driverId: String) -> Unit = {},
    onCopyPhoneNumber: (String) -> Unit = {},
    onMessagePhoneNumber: (String) -> Unit = {},
    onSendDriverInfoToClient: (driverName: String, driverPhoneNumber: String) -> Unit = {_, _ ->},
    onSendInfoToTravelCompany: (ride: Ride) -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row (
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.rides),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { onAddRide() }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Ride"
                )
            }
        }
        AnimatedContent(targetState = rides.isEmpty()) { state ->
            when{
                state -> {
                    Text(
                        modifier = Modifier.padding(top = 36.dp),
                        text = stringResource(R.string.no_rides_added),
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(rides){ ride ->
                            //TODO: implement swipe to delete
                            RideItem(
                                modifier = Modifier.animateItem(),
                                ride = ride,
                                onDeleteRide = {
                                    onDeleteRide(ride.id)
                                },
                                onCompanyClicked = {
                                    onCompanyClicked(ride.travelCompanyId)
                                },
                                onDriverClicked = {
                                    onDriverClicked(ride.driverId)
                                },
                                onCopyPhoneNumber = { number ->
                                    onCopyPhoneNumber(number)
                                },
                                onMessagePhoneNumber = { number ->
                                    onMessagePhoneNumber(number)
                                },
                                onSendDriverInfoToClient = {
                                    onSendDriverInfoToClient(ride.driver, ride.driverPhoneNumber)
                                },
                                onSendInfoToTravelCompany = {
                                    onSendInfoToTravelCompany(ride)
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
        val rides = listOf(
            Ride(
                id = "tristique",
                reservationId = "oratio",
                date = 7041,
                type = "Mazarat El Madina",
                car = "Camaro",
                travelCompanyPhone = "(398) 742-4872",
                driver = "Ahmed Mohsen",
                travelCompany = "Gawhara Travel Company",
                startLocation = "Gadda",
                endLocation = "Riyadh",
                buyingPrice = 0.1,
                sellingPrice = 2.3,
                collectedPrice = 4.5,
                note = "unum unum unum unum unum unum unum unum unum unum unum unum unum unum unum unum unum ",
                sentDriverInfoToCustomer = false,
                sentToDriverCompany = true
            ),
            Ride(
                id = "tristique",
                reservationId = "oratio",
                date = 7041,
                type = "Mazarat El Madina",
                car = "Camaro",
                travelCompanyPhone = "(398) 742-4872",
                driver = "Ahmed Mohsen",
                travelCompany = "Gawhara Travel Company",
                startLocation = "Gadda",
                endLocation = "Riyadh",
                buyingPrice = 0.1,
                sellingPrice = 2.3,
                collectedPrice = 4.5,
                note = "unum unum unum unum unum unum unum unum unum unum unum unum unum unum unum unum unum ",
                sentDriverInfoToCustomer = false,
                sentToDriverCompany = true
            )
        )
        RidesList(
            modifier = Modifier.padding(16.dp),
            rides = emptyList()
        )
    }
}