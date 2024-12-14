package com.zaed.reservationmanager.ui.reservation.display

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.data.model.Ride
import com.zaed.reservationmanager.ui.reservation.create.component.toSeconds
import com.zaed.reservationmanager.ui.reservation.details.components.RideItem
import com.zaed.reservationmanager.ui.reservation.display.component.DateRangePickerModal
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme
import com.zaed.reservationmanager.ui.util.PhoneUtil
import com.zaed.reservationmanager.ui.util.formatEpochSecondsToDate
import com.zaed.reservationmanager.ui.util.formatEpochSecondsToDateTime
import com.zaed.reservationmanager.ui.util.formatMoney
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId


@Composable
fun DisplayReservationScreen(
    viewModel: DisplayReservationViewModel = koinViewModel(),
    navigateToAddReservation: () -> Unit = {},
    onShowNavDrawer: () -> Unit = {},
    navigateToReservationDetails: (String) -> Unit = {},
    onNavigateToEmployeeDetails: (String, Boolean) -> Unit = { _, _ -> },
    navigateToEditReservation: (Reservation) -> Unit = {},
    navigateToCompanyDetails: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    DisplayReservationScreenContent(
        rides = state.rides.sortedBy { it.date },
        reservations = state.reservations,
        onNavigateToEditReservation={
            navigateToEditReservation(it)
        },
        onSendInfoToTravelCompany = { ride, reservation ->
            val messageText = context.getString(
                R.string.transportation_details,
                ride.travelCompany,
                reservation.clientName,
                reservation.clientPhone,
                ride.date.formatEpochSecondsToDateTime(),
                ride.startLocation,
                ride.endLocation,
                reservation.flightNumber,
                ride.buyingPrice.formatMoney(),
                ride.collectedPrice.formatMoney()
            )
            if(ride.travelCompanyPhone.isBlank()){
                scope.launch {
                    snackbarHostState.showSnackbar(context.getString(R.string.travel_company_phone_number_is_not_set))
                }
            }else {
                PhoneUtil.sendWhatsappMessage(
                    context = context,
                    phoneNumber = ride.travelCompanyPhone,
                    message = messageText,
                    onSuccess = {
                        viewModel.handleAction(
                            DisplayReservationUIAction.OnInfoSentToTravelCompany(
                                ride.id
                            )
                        )
                    },
                    onFailure = {
                        scope.launch {
                            snackbarHostState.showSnackbar(context.getString(R.string.whatsapp_is_not_installed))
                        }
                    }
                )
            }
        },
        onDeleteReservation = {
            viewModel.handleAction(DisplayReservationUIAction.OnDeleteReservation(it))
        },
        navigateToReservationDetails = navigateToReservationDetails,
        onNavigateToAddReservation = navigateToAddReservation,
        onShowNavDrawer = onShowNavDrawer,
        snackbarHostState = snackbarHostState,
        action = viewModel::handleAction,
        OnSendDriverInfoToCustomer = { rideId, clientPhone, driverName, driverPhoneNumber ->
            val messageText = context.getString(
                R.string.it_is_our_pleasure_to_serve_you_your_driver_can_be_reached_at_wishing_you_a_safe_and_pleasant_journey_god_willing,
                driverName,
                driverPhoneNumber
            )
            PhoneUtil.sendWhatsappMessage(
                context = context,
                phoneNumber = clientPhone,
                message = messageText,
                onSuccess = {
                    viewModel.handleAction(DisplayReservationUIAction.OnDriverInfoSent(rideId))
                },
                onFailure = {
                    scope.launch {
                        snackbarHostState.showSnackbar(context.getString(R.string.whatsapp_is_not_installed))
                    }
                }
            )
        },
        onDriverClicked = {
            onNavigateToEmployeeDetails(it, true)
        },
        onCopyPhoneNumber = {
            clipboardManager.setText(AnnotatedString(it))
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.copied_to_clipboard),
                    withDismissAction = true
                )
            }
        },
        onMessagePhoneNumber = {
            PhoneUtil.sendWhatsappMessage(
                context = context,
                phoneNumber = it,
                message = "",
                onFailure = {
                    scope.launch {
                        snackbarHostState.showSnackbar(context.getString(R.string.whatsapp_is_not_installed))
                    }
                }
            )
        },
        navigateToCompanyDetails = navigateToCompanyDetails
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayReservationScreenContent(
    rides: List<Ride> = emptyList(),
    reservations: List<Reservation> = emptyList(),
    onNavigateToAddReservation: () -> Unit = {},
    OnSendDriverInfoToCustomer: (rideId: String, clientPhone: String, driverName: String, driverPhoneNumber: String) -> Unit = { _, _, _, _ -> },
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onShowNavDrawer: () -> Unit = {},
    action: (DisplayReservationUIAction) -> Unit = {},
    onMessagePhoneNumber: (String) -> Unit = {},
    navigateToReservationDetails: (String) -> Unit = {},
    onCopyPhoneNumber: (String) -> Unit = {},
    onDriverClicked: (driverId: String) -> Unit = {},
    onSendInfoToTravelCompany: (ride: Ride, reservation: Reservation) -> Unit = { _, _ -> },
    onDeleteReservation: (String) -> Unit = {},
    onNavigateToEditReservation:(Reservation) -> Unit = {},
    navigateToCompanyDetails: (String) -> Unit = {}
) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(stringResource(R.string.reservations))
                },
                navigationIcon = {
                    IconButton(onClick = onShowNavDrawer) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = null)
                    }

                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddReservation
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            var selectedDate by remember { mutableStateOf("") }
            var dateRangeStart by remember { mutableStateOf<Long?>(null) }
            var dateRangeEnd by remember { mutableStateOf<Long?>(null) }
            var showDateRangePicker by remember { mutableStateOf(false) }
            val localDate = LocalDate.now()
            val startOfDay: LocalDateTime = localDate.atStartOfDay()
            val startOfToday = startOfDay.atZone(ZoneId.systemDefault()).toInstant().epochSecond
            val endOfToday = startOfToday + 86400
            val startOfTomorrow = endOfToday + 1
            val endOfTomorrow = startOfTomorrow + 86400
            val startOfYesterday = startOfToday - 86400
            val endOfYesterday = startOfToday - 1
            var searchQuery by remember { mutableStateOf("") }
            var state by remember { mutableStateOf(0) }
            val titles = listOf(stringResource(R.string.reservations), stringResource(R.string.rides))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(stringResource(R.string.search_by_anything)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )

            val filteredRides1 = rides.filter { ride ->
                val reservation = reservations.find { it.id == ride.reservationId }
                listOfNotNull(
                    ride.car,
                    ride.driver,
                    ride.travelCompany,
                    ride.startLocation,
                    ride.endLocation,
                    ride.type,
                    reservation?.flightNumber,
                    reservation?.clientName,
                    reservation?.clientPhone,
                    reservation?.clientCountry,
                    reservation?.tourismCompany,
                    reservation?.tourismCompanyPhone,
                    reservation?.tourismEmployee,
                    reservation?.tourismEmployeePhone
                ).any { field ->
                    field.contains(searchQuery, ignoreCase = true)
                }
            }
            val filteredReservation1 = reservations.filter { reservation ->
                listOfNotNull(
                    reservation.flightNumber,
                    reservation.clientName,
                    reservation.clientPhone,
                    reservation.clientCountry,
                    reservation.tourismCompany,
                    reservation?.tourismCompanyPhone,
                    reservation?.tourismEmployee,
                    reservation?.tourismEmployeePhone
                ).any { field ->
                    field.contains(searchQuery, ignoreCase = true)
                }
            }
            val items = listOf(
                stringResource(R.string.today),
                stringResource(R.string.yesterday),
                stringResource(R.string.tomorrow),
                stringResource(R.string.from_today_onwards)
            )
            LazyRow {
                items(
                    items = items
                ) { date ->
                    FilterChip(
                        modifier = Modifier.padding(end = 8.dp),
                        onClick = {
                            selectedDate = if (selectedDate == date) "" else date
                        },
                        label = { Text(date) },
                        selected = selectedDate == date,
                        leadingIcon = if (selectedDate == date) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Done,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        } else {
                            null
                        },
                    )
                }
                item {
                    FilterChip(
                        modifier = Modifier.padding(end = 8.dp),
                        onClick = {
                            if (dateRangeStart != null && dateRangeEnd != null) {
                                dateRangeStart = null
                                dateRangeEnd = null
                            } else {
                                selectedDate = ""
                                showDateRangePicker = true
                            }
                        },
                        label = { Text(stringResource(R.string.selected_range)) },
                        selected = dateRangeStart != null && dateRangeEnd != null,
                        leadingIcon = if (dateRangeStart != null && dateRangeEnd != null) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Done,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        } else {
                            null
                        },
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (dateRangeStart != null && dateRangeEnd != null) {
                Text(text = stringResource(
                    R.string.selected_range_place,
                    dateRangeStart!!.formatEpochSecondsToDate(),
                    dateRangeEnd!!.formatEpochSecondsToDate()
                ))
            }

            val filteredRides2 = if (selectedDate.isNotBlank()) {
                if (selectedDate == items[1]) {
                    filteredRides1.filter { it.date in startOfYesterday..endOfYesterday }
                } else if (selectedDate == items[2]) {
                    filteredRides1.filter { it.date in startOfTomorrow..endOfTomorrow }
                } else if (selectedDate == items[0]) {
                    filteredRides1.filter { it.date in startOfToday..endOfToday }

                } else if (selectedDate == items[3]) {
                    filteredRides1.filter { it.date >= startOfToday }
                } else {
                    filteredRides1
                }
            } else if (dateRangeStart != null && dateRangeEnd != null) {
                filteredRides1.filter { it.date in dateRangeStart!!..dateRangeEnd!! }
            } else {
                filteredRides1
            }
            val filteredReservations2 = if (selectedDate.isNotBlank()) {
                if (selectedDate == items[1]) {
                    filteredReservation1.filter { it.date in startOfYesterday..endOfYesterday }
                } else if (selectedDate == items[2]) {
                    filteredReservation1.filter { it.date in startOfTomorrow..endOfTomorrow }
                } else if (selectedDate == items[0]) {
                    filteredReservation1.filter { it.date in startOfToday..endOfToday }

                } else if (selectedDate == items[3]) {
                    filteredReservation1.filter { it.date >= startOfToday }
                } else {
                    filteredReservation1
                }
            } else if (dateRangeStart != null && dateRangeEnd != null) {
                filteredReservation1.filter { it.date in dateRangeStart!!..dateRangeEnd!! }
            } else {
                filteredReservation1
            }
            PrimaryTabRow(selectedTabIndex = state) {
                titles.forEachIndexed { index, title ->
                    Tab(
                        selected = state == index,
                        onClick = { state = index },
                        text = {
                            Text(
                                text = title,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }
            if (state == 0) ReservationList(
                reservations = filteredReservations2.sortedBy { it.date },
                onNavigateToReservationDetails = navigateToReservationDetails,
                onDeleteReservation = onDeleteReservation,
                onNavigateToEditReservation = onNavigateToEditReservation
            )
            else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 16.dp),
                ) {
                    items(filteredRides2) { ride ->
                        RideItem(
                            ride = ride,
                            onDeleteRide = {
                                action(DisplayReservationUIAction.OnDeleteRide(ride.id))
                            },
                            onReservationClicked = {
                                navigateToReservationDetails(ride.reservationId)
                            },
                            onCompanyClicked = navigateToCompanyDetails,
                            onMessagePhoneNumber = onMessagePhoneNumber,
                            onCopyPhoneNumber = onCopyPhoneNumber,
                            onDriverClicked = {
                                onDriverClicked(ride.driverId)
                            },
                            onSendDriverInfoToClient = {
                                OnSendDriverInfoToCustomer(
                                    ride.id,
                                    reservations.find { it.id == ride.reservationId }?.clientPhone
                                        ?: "",
                                    ride.driver,
                                    ride.driverPhoneNumber
                                )
                            },
                            onSendInfoToTravelCompany = {
                                onSendInfoToTravelCompany(
                                    ride,
                                    reservations.find { it.id == ride.reservationId }
                                        ?: Reservation()
                                )
                            },
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }


            if (showDateRangePicker) {
                DateRangePickerModal(
                    onDateRangeSelected = {
                        dateRangeStart = it.first?.toSeconds()
                        dateRangeEnd = it.second?.toSeconds()
                        selectedDate = ""
                    },
                    onDismiss = { showDateRangePicker = false }
                )
            }
        }

    }

}

@Composable
fun ReservationList(
    modifier: Modifier = Modifier,
    reservations: List<Reservation>,
    onNavigateToReservationDetails: (String) -> Unit = {},
    onDeleteReservation: (String) -> Unit = {},
    onNavigateToEditReservation: (Reservation) -> Unit = {}
) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 16.dp),
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(reservations) { reservation ->
            ExpandableReservationCard(
                reservation = reservation,
                onDeleteClicked = {onDeleteReservation(reservation.id)},
                onNavigateToEditReservation = onNavigateToEditReservation,
                onNavigateToReservationDetails = onNavigateToReservationDetails,

            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableReservationCard(
    reservation: Reservation,
    onDeleteClicked: () -> Unit = {},
    onNavigateToEditReservation: (Reservation) -> Unit = {},
    onNavigateToReservationDetails: (reservationId: String) -> Unit = {}

) {
    var expanded by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        onClick = { expanded = !expanded },
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, end = 16.dp, start = 16.dp, bottom = 0.dp)
        ) {
            AnimatedVisibility(!expanded) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.reservation_number),
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "#${reservation.reservationNumber}",
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp, horizontal = 2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.customer_name),
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = reservation.clientName,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            AnimatedVisibility(expanded) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.name),
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = reservation.clientName,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp, horizontal = 2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.phone_number),
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = reservation.clientPhone,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp, horizontal = 2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.country),
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = reservation.clientCountry,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp, horizontal = 2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.tourism_company),
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = reservation.tourismCompany,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp, horizontal = 2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.tourism_employee),
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = reservation.tourismEmployee,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp, horizontal = 2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.company_phone),
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = reservation.tourismCompanyPhone,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp, horizontal = 2.dp))



                    TextButton(
                        contentPadding = PaddingValues(0.dp),
                        onClick = {onNavigateToReservationDetails(reservation.id)},
                    ) {
                        Text(text = stringResource(R.string.reservation_details_arrow))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(
                            contentPadding = PaddingValues(0.dp),
                            onClick = onDeleteClicked,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.delete),
                                modifier = Modifier.wrapContentWidth()
                            )
                        }


                        TextButton(
                            contentPadding = PaddingValues(0.dp),
                            onClick = {onNavigateToEditReservation(reservation)},
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.edit),
                                modifier = Modifier.wrapContentWidth()
                            )
                        }
                    }
                }
            }
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
            )

        }
    }
}

@Composable
@Preview
fun CustomerListScreenPreview() {
    ReservationManagerTheme {
        DisplayReservationScreenContent(
            rides = listOf(
                Ride(),
                Ride(),
                Ride()
            ),
            reservations = listOf(
                Reservation(
                    clientName = "ali",
                    clientPhone = "+201029184550"
                ),
                Reservation(),
                Reservation()

            )

        )
    }
}