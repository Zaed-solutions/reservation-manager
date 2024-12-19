package com.zaed.reservationmanager.ui.home.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Whatsapp
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import com.zaed.reservationmanager.data.model.CompanyType
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme
import com.zaed.reservationmanager.ui.util.formatEpochSecondsToDate
import com.zaed.reservationmanager.ui.util.formatEpochSecondsToDateTime
import com.zaed.reservationmanager.ui.util.formatMoney

@Composable
fun ReservationItem(
    modifier: Modifier = Modifier,
    reservation: Reservation = Reservation(),
    onDeleteReservation: () -> Unit = {},
    onCompanyClicked: (String, CompanyType) -> Unit = { _, _ -> },
    onMessagePhoneNumber: (String) -> Unit = {},
    onCopyPhoneNumber: (String) -> Unit = {},
    onSendDriverInfoToClient: () -> Unit = {},
    onSendInfoToTravelCompany: () -> Unit = {},
    onSendConfirmationToCustomer: () -> Unit = {},
    onReservationClicked: () -> Unit = {},
    onEditReservation: () -> Unit = {},
    isActionsVisible: Boolean = true,
    isEditable: Boolean = true,
) {
    var isExpanded by remember {
        mutableStateOf(true)
    }
    var isOptionMenuVisible by remember {
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
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Category,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    contentDescription = "Reservation Category",
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = reservation.type,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    contentDescription = "Reservation Date",
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(16.dp)
                )
                Text(
                    modifier = Modifier.weight(0.6f),
                    text = reservation.date.formatEpochSecondsToDateTime(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .wrapContentSize(Alignment.TopEnd)
                ) {
                    IconButton(
                        onClick = { isOptionMenuVisible = !isOptionMenuVisible },
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(24.dp)

                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = null,
                        )
                    }

                    DropdownMenu(
                        expanded = isOptionMenuVisible,
                        onDismissRequest = { isOptionMenuVisible = false }
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                onDeleteReservation()
                                isOptionMenuVisible = false
                            },
                            text = {
                                Text(
                                    text = stringResource(R.string.delete),
                                )
                            },
                        )
                        if (isEditable) {
                            DropdownMenuItem(
                                onClick = {
                                    onEditReservation()
                                    isOptionMenuVisible = false
                                },
                                text = {
                                    Text(
                                        text = stringResource(R.string.edit),
                                    )
                                },
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LocationItem(
                    modifier = Modifier.weight(1f),
                    location = reservation.startLocation
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowForwardIos,
                    contentDescription = "Arrow",
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                LocationItem(
                    modifier = Modifier.weight(1f),
                    location = reservation.endLocation
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = reservation.clientName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                VerticalDivider(
                    modifier = Modifier
                        .height(10.dp)
                        .padding(horizontal = 12.dp)
                )
                Text(
                    text = reservation.tourismCompany,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )


//                VerticalDivider(
//                    modifier = Modifier.height(20.dp).padding(horizontal = 8.dp)
//                )
//                Text(
//                    text = reservation.travelCompany,
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant,
//                    modifier = Modifier.weight(1f)
//                )
//                Text(
//                    text = reservation.driver,
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant,
//                    modifier = Modifier.weight(1f)
//                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DetailRow(
                        label = stringResource(R.string.reservation_number),
                        value = "#${reservation.reservationNumber}",
                        onClick = {
                            onReservationClicked()
                        }
                    )

                    DetailRow(
                        label = stringResource(R.string.tourism_company),
                        value = reservation.tourismCompany,
                        onClick = {
                            onCompanyClicked(reservation.tourismCompany, CompanyType.TOURISM)
                        }
                    )
                    DetailRow(
                        label = stringResource(R.string.tourism_employee),
                        value = reservation.tourismEmployee,
                        onClick = {
                            //TODO
                        }
                    )
                    DetailRow(
                        label = stringResource(R.string.tourism_employee),
                        value = reservation.tourismEmployeePhone,
                        onClick = {
                            onCopyPhoneNumber(reservation.tourismCompany)
                        }
                    )
                    DetailRow(
                        label = stringResource(id = R.string.phone_number),
                        value = reservation.travelCompanyPhone,
                        onClick = {
                            onMessagePhoneNumber(reservation.travelCompanyPhone)
                        },
                        onLongClick = {
                            onCopyPhoneNumber(reservation.travelCompanyPhone)
                        }
                    )
                    DetailRow(
                        label = stringResource(R.string.travel_company),
                        value = reservation.travelCompany,
                        onClick = {
                            onCompanyClicked(reservation.travelCompanyId, CompanyType.TRAVEL)
                        }
                    )
                    DetailRow(
                            label = stringResource(id = R.string.phone_number),
                    value = reservation.travelCompanyPhone,
                    onClick = {
                        onMessagePhoneNumber(reservation.travelCompanyPhone)
                    },
                    onLongClick = {
                        onCopyPhoneNumber(reservation.travelCompanyPhone)
                    }
                    )
                    DetailRow(
                            label = stringResource(id = R.string.driver),
                    value = reservation.driver,
                    )
                    DetailRow(
                        label = stringResource(id = R.string.driver),
                        value = reservation.driverPhoneNumber,
                        onClick = {
                            onMessagePhoneNumber(reservation.driverPhoneNumber)
                        },
                        onLongClick = {
                            onCopyPhoneNumber(reservation.driverPhoneNumber)
                        }
                    )
                    DetailRow(
                        label = stringResource(R.string.car),
                        value = reservation.car
                    )


                    DetailRow(
                        label = stringResource(R.string.buying_price),
                        value = reservation.buyingPrice.formatMoney(),
                    )
                    DetailRow(
                        label = stringResource(R.string.selling_price),
                        value = reservation.sellingPrice.formatMoney(),
                    )
                    DetailRow(
                        label = stringResource(R.string.collected_price),
                        value = reservation.collectedAmount.formatMoney(),
                    )
                    DetailRow(
                        label = stringResource(R.string.notes),
                        value = reservation.note,
                        isDividerVisible = false,
                        isValueSingleLine = false
                    )
                    if (isActionsVisible) {
                        Column(
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Button(
                                enabled = !reservation.sentConfirmToCustomer,
                                onClick = { onSendConfirmationToCustomer() },
                                contentPadding = PaddingValues(
                                    horizontal = 16.dp,
                                    vertical = 8.dp
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = if (reservation.sentConfirmToCustomer) Icons.Default.Check else Icons.Default.Whatsapp,
                                    contentDescription = null,
                                    tint = if (reservation.sentConfirmToCustomer)
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    else
                                        MaterialTheme.colorScheme.onPrimary
                                )
                                Text(
                                    modifier = Modifier.padding(start = 8.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodyMedium,
                                    text = stringResource(
                                        if (reservation.sentConfirmToCustomer)
                                            R.string.confirmation_sent
                                        else
                                            R.string.send_confirmation_to_client
                                    )
                                )
                            }

                            Button(
                                enabled = !reservation.sentDriverInfoToCustomer,
                                onClick = { onSendDriverInfoToClient() },
                                contentPadding = PaddingValues(
                                    horizontal = 16.dp,
                                    vertical = 8.dp
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = if (reservation.sentDriverInfoToCustomer) Icons.Default.Check else Icons.Default.Whatsapp,
                                    contentDescription = null,
                                    tint = if (reservation.sentDriverInfoToCustomer)
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    else
                                        MaterialTheme.colorScheme.onPrimary
                                )
                                Text(
                                    modifier = Modifier.padding(start = 8.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodyMedium,
                                    text = stringResource(
                                        if (reservation.sentDriverInfoToCustomer)
                                            R.string.driver_information_sent
                                        else
                                            R.string.send_info_to_client
                                    )
                                )
                            }
                            if (reservation.driver.isNotBlank()) {
                                Button(
                                    enabled = !reservation.sentToDriverCompany,
                                    onClick = { onSendInfoToTravelCompany() },
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    contentPadding = PaddingValues(
                                        horizontal = 16.dp,
                                        vertical = 8.dp
                                    )
                                ) {
                                    Icon(
                                        imageVector = if (reservation.sentToDriverCompany) Icons.Default.Check else Icons.Default.Whatsapp,
                                        contentDescription = null,
                                        tint = if (reservation.sentToDriverCompany) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                                    )
                                    Text(
                                        modifier = Modifier.padding(start = 8.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        text = stringResource(
                                            if (reservation.sentToDriverCompany)
                                                R.string.reservation_info_sent
                                            else
                                                R.string.send_reservation_info_to_travel_company
                                        )
                                    )
                                }
                            }
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
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            contentDescription = "Location",
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = location,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Preview(showSystemUi = false, showBackground = true)
@Composable
private fun Preview() {
    ReservationManagerTheme {
        ReservationItem(
            modifier = Modifier.padding(16.dp),
            reservation = Reservation(
                id = "tristique",
                date = 7041,
                type = "Mazarat El Madina",
                clientName = "Ahmed Mohsen",
                tourismCompany = "Gawhara Tourism Company",
                car = "Camaro",
                tourismEmployee = "kkkkkkkk",
                travelCompanyPhone = "(398) 742-4872",
                driver = "Ahmed Mohsen",
                travelCompany = "Gawhara Travel Company",
                startLocation = "Gadda",
                endLocation = "Riyadh",
                buyingPrice = 0.1,
                driverPhoneNumber = "0000000000000",
                sellingPrice = 2.3,
                collectedAmount = 4.5,
                note = "unum unum n unum ",
                sentDriverInfoToCustomer = false,
                sentToDriverCompany = true
            )
        )
    }
}