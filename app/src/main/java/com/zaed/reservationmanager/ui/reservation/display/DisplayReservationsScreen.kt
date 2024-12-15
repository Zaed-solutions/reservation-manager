package com.zaed.reservationmanager.ui.reservation.display

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.core.content.FileProvider
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.data.model.Ride
import com.zaed.reservationmanager.ui.company.display.components.ConfirmDeleteDialog
import com.zaed.reservationmanager.ui.reservation.create.component.toSeconds
import com.zaed.reservationmanager.ui.reservation.details.components.RideItem
import com.zaed.reservationmanager.ui.reservation.display.component.DateFixedPickerModal
import com.zaed.reservationmanager.ui.reservation.display.component.DateRangePickerModal
import com.zaed.reservationmanager.ui.reservation.display.component.ReservationList
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme
import com.zaed.reservationmanager.ui.util.PhoneUtil
import com.zaed.reservationmanager.ui.util.SheetUtil.exportRidesAsCsv
import com.zaed.reservationmanager.ui.util.formatEpochSecondsToDate
import com.zaed.reservationmanager.ui.util.formatEpochSecondsToDateTime
import com.zaed.reservationmanager.ui.util.formatMoney
import com.zaed.reservationmanager.ui.util.getStartAndEndOfDay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


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
        onNavigateToEditReservation = {
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
            if (ride.travelCompanyPhone.isBlank()) {
                scope.launch {
                    snackbarHostState.showSnackbar(context.getString(R.string.travel_company_phone_number_is_not_set))
                }
            } else {
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
        navigateToCompanyDetails = navigateToCompanyDetails,
        exportRidesAsCsv = {
            val file = state.rides.exportRidesAsCsv(
                context = context,
                isAllRides = true,
                headers = listOf(
                    context.getString(R.string.date),
                    context.getString(R.string.type),
                    context.getString(R.string.car),
                    context.getString(R.string.client_name),
                    context.getString(R.string.selling_price),
                    context.getString(R.string.buying_price),
                    context.getString(R.string.collected_price),
                    context.getString(R.string.balance)
                )
            )
            scope.launch {
                if (file != null) {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.pdf_saved_at, file.path),
                        actionLabel = context.getString(R.string.open)
                    ).let { result ->
                        if (result == SnackbarResult.ActionPerformed) {
                            try {
                                val openFileIntent = Intent(Intent.ACTION_VIEW).apply {
                                    val fileUri: Uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )
                                    setDataAndType(fileUri, "text/csv")
                                    flags =
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                if (openFileIntent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(openFileIntent)
                                } else {
                                    snackbarHostState.showSnackbar(context.getString(R.string.no_csv_viewer_found))
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                } else {
                    snackbarHostState.showSnackbar(context.getString(R.string.error_exporting_csv))
                }
            }
        }
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
    onNavigateToEditReservation: (Reservation) -> Unit = {},
    navigateToCompanyDetails: (String) -> Unit = {},
    exportRidesAsCsv: () -> Unit = {}
) {
    var isOptionsMenuVisible by remember {
        mutableStateOf(false)
    }
    var isConfirmDeleteDialogVisible by remember {
        mutableStateOf(false)
    }
    var selectedItemId by remember {
        mutableStateOf("")
    }
    var isRide by remember {
        mutableStateOf(false)
    }
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

                },
                actions = {
                    Box(
                        modifier = Modifier
                            .wrapContentSize(Alignment.TopEnd)
                    ) {
                        IconButton(
                            onClick = { isOptionsMenuVisible = !isOptionsMenuVisible },
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = null,
                            )
                        }
                        DropdownMenu(
                            expanded = isOptionsMenuVisible,
                            onDismissRequest = { isOptionsMenuVisible = false }
                        ) {
                            DropdownMenuItem(
                                onClick = {
                                    exportRidesAsCsv()
                                    isOptionsMenuVisible = false
                                },
                                text = {
                                    Text(
                                        text = stringResource(R.string.export_as_csv),
                                    )
                                },
                            )
//                            DropdownMenuItem(
//                                onClick = {
//                                    onExportCustomersAsPDF()
//                                    isOptionsMenuVisible = false
//                                },
//                                text = {
//                                    Text(
//                                        text = stringResource(R.string.export_as_pdf),
//                                    )
//                                },
//                            )
                        }
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
            val items = listOf(
                stringResource(R.string.yesterday),
                stringResource(R.string.today),
                stringResource(R.string.tomorrow),
                stringResource(R.string.from_today_onwards),
            )
            var selectedDate by remember { mutableStateOf(items[1]) }
            var selectedFixedDate by remember { mutableStateOf<Long?>(null) }
            var dateRangeStart by remember { mutableStateOf<Long?>(null) }
            var dateRangeEnd by remember { mutableStateOf<Long?>(null) }
            var showDateRangePicker by remember { mutableStateOf(false) }
            var showFixedDatePicker by remember { mutableStateOf(false) }
            val currentEpochSecond = System.currentTimeMillis() / 1000
            val (startOfToday, endOfToday) = getStartAndEndOfDay(currentEpochSecond)
            val startOfTomorrow = endOfToday + 1
            val endOfTomorrow = startOfTomorrow + 86400
            val startOfYesterday = startOfToday - 86400
            val endOfYesterday = startOfToday - 1
            var searchQuery by remember { mutableStateOf("") }
            var state by remember { mutableStateOf(0) }
            val titles =
                listOf(stringResource(R.string.reservations), stringResource(R.string.rides))
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
                item {
                    FilterChip(
                        modifier = Modifier.padding(end = 8.dp),
                        onClick = {
                            if (selectedFixedDate != null) {
                                selectedFixedDate = null
                            } else {
                                selectedDate = ""
                                showFixedDatePicker = true
                            }
                        },
                        label = { Text(stringResource(R.string.selected_date)) },
                        selected = selectedFixedDate != null,
                        leadingIcon = if (selectedFixedDate != null) {
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
                Text(
                    text = stringResource(
                        R.string.selected_range_place,
                        dateRangeStart!!.formatEpochSecondsToDate(),
                        dateRangeEnd!!.formatEpochSecondsToDate()
                    )
                )
            }

            val filteredRides2 = if (selectedDate.isNotBlank()) {
                if (selectedDate == items[0]) {
                    filteredRides1.filter { it.date in startOfYesterday..endOfYesterday }
                } else if (selectedDate == items[2]) {
                    filteredRides1.filter { it.date in startOfTomorrow..endOfTomorrow }
                } else if (selectedDate == items[1]) {
                    filteredRides1.filter { it.date in startOfToday..endOfToday }

                } else if (selectedDate == items[3]) {
                    filteredRides1.filter { it.date >= startOfToday }
                } else {
                    filteredRides1
                }
            } else if (dateRangeStart != null && dateRangeEnd != null) {

                filteredRides1.filter {
                    it.date in getStartAndEndOfDay(dateRangeStart!!).first..getStartAndEndOfDay(
                        dateRangeEnd!!
                    ).second
                }
            } else if (selectedFixedDate != null) {
                val (start, end) = getStartAndEndOfDay(selectedFixedDate!!)
                filteredRides1.filter { it.date in start..end }
            } else {
                filteredRides1
            }
            val filteredReservations2 = if (selectedDate.isNotBlank()) {
                if (selectedDate == items[0]) {
                    filteredReservation1.filter { it.date in startOfYesterday..endOfYesterday }
                } else if (selectedDate == items[2]) {
                    filteredReservation1.filter { it.date in startOfTomorrow..endOfTomorrow }
                } else if (selectedDate == items[1]) {
                    filteredReservation1.filter { it.date in startOfToday..endOfToday }

                } else if (selectedDate == items[3]) {
                    filteredReservation1.filter { it.date >= startOfToday }
                }  else {
                    filteredReservation1
                }
            } else if (dateRangeStart != null && dateRangeEnd != null) {
                filteredReservation1.filter { it.date in dateRangeStart!!..dateRangeEnd!! }
            }else if (selectedFixedDate != null) {
                val (start, end) = getStartAndEndOfDay(selectedFixedDate!!)
                filteredReservation1.filter { it.date in start..end }
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
                onDeleteReservation = {reservationId ->
                    isConfirmDeleteDialogVisible = true
                    selectedItemId = reservationId
                    isRide = false
                },
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
                                isConfirmDeleteDialogVisible = true
                                selectedItemId = ride.id
                                isRide = true
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
            if (showFixedDatePicker) {
                DateFixedPickerModal(
                    onDateSelected = {
                        selectedFixedDate = it
                        selectedDate = ""
                    },
                    onDismiss = { showFixedDatePicker = false }
                )
            }
            AnimatedVisibility(isConfirmDeleteDialogVisible) {
                ModalBottomSheet(
                    onDismissRequest = {
                        isConfirmDeleteDialogVisible = false
                        selectedItemId = ""
                    },
                    sheetState = rememberModalBottomSheetState()
                ) {
                    ConfirmDeleteDialog(
                        label = if (isRide) stringResource(id = R.string.ride) else stringResource(
                            id = R.string.reservation
                        ),
                        onDismiss = {
                            isConfirmDeleteDialogVisible = false
                            selectedItemId = ""
                        },
                        onConfirm = {
                            action(
                                if (isRide)
                                    DisplayReservationUIAction.OnDeleteRide(selectedItemId)
                                else
                                    DisplayReservationUIAction.OnDeleteReservation(selectedItemId)
                            )
                            isConfirmDeleteDialogVisible = false
                            selectedItemId = ""
                        }
                    )
                }
            }

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