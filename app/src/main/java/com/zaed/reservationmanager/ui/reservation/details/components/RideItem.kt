package com.zaed.reservationmanager.ui.reservation.details.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoubleArrow
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Whatsapp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Ride
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme

@Composable
fun RideItem(
    modifier: Modifier = Modifier,
    ride: Ride = Ride(),
    onDeleteRide: () -> Unit = {},
    onCompanyClicked: () -> Unit = {},
    onMessagePhoneNumber: (String) -> Unit = {},
    onCopyPhoneNumber: (String) -> Unit = {},
    onDriverClicked: () -> Unit = {},
    onSendDriverInfoToClient: () -> Unit = {},
    onSendInfoToTravelCompany: () -> Unit = {},
) {
    var isExpanded by remember {
        mutableStateOf(false)
    }
    val anim = remember {
        Animatable(0f)
    }
    LaunchedEffect(isExpanded) {
        anim.animateTo(
            targetValue = if (isExpanded) 180f else 0f
        )
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        onClick = { isExpanded = !isExpanded },
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Category,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    contentDescription = "Ride Category",
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = ride.type,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LocationItem(
                    location = ride.startLocation
                )
                Icon(
                    imageVector = Icons.Default.DoubleArrow,
                    contentDescription = "Arrow",
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                LocationItem(
                    location = ride.endLocation
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    contentDescription = "Ride Date",
                    modifier = Modifier.size(16.dp)
                )
                //TODO: format date
                Text(
                    text = ride.date.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DetailRow(
                        label = stringResource(R.string.travel_company),
                        value = ride.travelCompany,
                        onClick = {
                            onCompanyClicked()
                        }
                    )
                    DetailRow(
                        label = stringResource(id = R.string.phone_number),
                        value = ride.travelCompanyPhone,
                        onClick = {
                            onMessagePhoneNumber(ride.travelCompanyPhone)
                        },
                        onLongClick = {
                            onCopyPhoneNumber(ride.travelCompanyPhone)
                        }
                    )
                    DetailRow(
                        label = stringResource(R.string.car),
                        value = ride.car
                    )
                    DetailRow(
                        label = stringResource(id = R.string.driver),
                        value = ride.driver,
                        onClick = {
                            onDriverClicked()
                        }
                    )
                    DetailRow(
                        label = stringResource(id = R.string.phone_number),
                        value = ride.travelCompanyPhone,
                        onClick = {
                            onMessagePhoneNumber(ride.travelCompanyPhone)
                        },
                        onLongClick = {
                            onCopyPhoneNumber(ride.travelCompanyPhone)
                        }
                    )
                    /*
                    * TODO: Add Prices Table
                    */
                    DetailRow(
                        label = stringResource(R.string.notes),
                        value = ride.note
                    )
                    Column(
                        modifier = Modifier.padding(top = 8.dp)
                    ) {

                        Button(
                            enabled = !ride.sentDriverInfoToCustomer,
                            onClick = { onSendDriverInfoToClient() },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = if (ride.sentDriverInfoToCustomer) Icons.Default.Check else Icons.Default.Whatsapp,
                                contentDescription = null,
                                tint = if (ride.sentDriverInfoToCustomer)
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                else
                                    MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                modifier = Modifier.padding(start = 8.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                text = stringResource(
                                    if (ride.sentDriverInfoToCustomer)
                                        R.string.driver_information_sent
                                    else
                                        R.string.send_info_to_client
                                )
                            )
                        }
                        Button(
                            enabled = !ride.sentToDriverCompany,
                            onClick = { onSendInfoToTravelCompany() },
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = if (ride.sentToDriverCompany) Icons.Default.Check else Icons.Default.Whatsapp,
                                contentDescription = null,
                                tint = if (ride.sentToDriverCompany) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                modifier = Modifier.padding(start = 8.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                text = stringResource(
                                    if (ride.sentToDriverCompany)
                                        R.string.ride_info_sent
                                    else
                                        R.string.send_ride_info_to_travel_company
                                )
                            )
                        }
                    }
                }
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.rotate(anim.value)
            )
        }
    }
}

@Composable
private fun LocationItem(
    modifier: Modifier = Modifier,
    location: String = "",
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            contentDescription = "Location",
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = location,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun Preview() {
    ReservationManagerTheme {
        RideItem(
            modifier = Modifier.padding(16.dp),
            ride = Ride(
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
        ) {

        }
    }
}