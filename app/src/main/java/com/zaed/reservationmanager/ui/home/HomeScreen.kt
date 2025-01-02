package com.zaed.reservationmanager.ui.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.CompanyType
import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.ui.company.display.components.ConfirmDeleteDialog
import com.zaed.reservationmanager.ui.home.component.AddReservationBottomSheetContent
import com.zaed.reservationmanager.ui.home.component.CustomerListWithTitle
import com.zaed.reservationmanager.ui.home.component.DateFixedPickerModal
import com.zaed.reservationmanager.ui.home.component.DateRangePickerModal
import com.zaed.reservationmanager.ui.home.component.ReservationsList
import com.zaed.reservationmanager.ui.home.component.TimeFilter
import com.zaed.reservationmanager.ui.home.component.TimeFiltersChips
import com.zaed.reservationmanager.ui.home.component.getTransportationDetailsMessage
import com.zaed.reservationmanager.ui.reservation.create.component.toSeconds
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme
import com.zaed.reservationmanager.ui.util.PhoneUtil
import com.zaed.reservationmanager.ui.util.SheetUtil.exportCustomersToExcel
import com.zaed.reservationmanager.ui.util.SheetUtil.exportReservationsToExcel
import com.zaed.reservationmanager.ui.util.SheetUtil.importCustomersFromExcel
import com.zaed.reservationmanager.ui.util.formatEpochSecondsToDate
import com.zaed.reservationmanager.ui.util.formatEpochSecondsToMessageDateTime
import com.zaed.reservationmanager.ui.util.formatEpochSecondsToMonthlyDate
import com.zaed.reservationmanager.ui.util.formatMoney
import com.zaed.reservationmanager.ui.util.getDate
import com.zaed.reservationmanager.ui.util.showSnackbarWithDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.Locale


@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    onShowNavDrawer: () -> Unit = {},
    onNavigateToAddCustomer: () -> Unit = {},
    onNavigateToEditCustomer: (Customer) -> Unit = {},
    onNavigateToAddReservation: () -> Unit = {},
    onNavigateToCustomerDetails: (String) -> Unit = {},
    onNavigateToCompanyDetails: (String, CompanyType) -> Unit = { _, _ -> },
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    HomeScreenContent(
        reservations = state.displayedReservations,
        customers = state.displayedCustomers,
        searchQuery = state.searchQuery,
        selectedCountry = state.selectedCountry,
        selectedTimeFilter = state.timeFilter,
        reservationTypes = state.reservationTypes,
        cars = state.cars,
        tourismCompanies = state.tourismCompanies,
        employees = state.employees,
        travelCompanies = state.travelCompanies,
        drivers = state.drivers,
        countries = state.countries,
        isLoading = state.isLoading,
        snackbarHostState = snackbarHostState,
        scope = scope,
        onAction = { action ->
            when (action) {
                HomeUiAction.ShowNavDrawer -> onShowNavDrawer()
                HomeUiAction.AddCustomer -> onNavigateToAddCustomer()
                HomeUiAction.AddReservation -> onNavigateToAddReservation()
                is HomeUiAction.OnCompanyClicked -> onNavigateToCompanyDetails(
                    action.companyId,
                    action.companyType
                )

                is HomeUiAction.OnEditCustomerClicked -> onNavigateToEditCustomer(action.customer)
                is HomeUiAction.OnViewCustomerDetails -> onNavigateToCustomerDetails(action.customerId)
                HomeUiAction.ExportCustomersAsCsv -> {
                    val file = state.displayedCustomers.exportCustomersToExcel(
                        context = context,
                        headers = listOf(
                            context.getString(R.string.name),
                            context.getString(R.string.nationality),
                            context.getString(R.string.residence_country),
                            context.getString(R.string.email),
                            context.getString(R.string.phone_number)
                        )
                    )
                    scope.launch {
                        if (file != null) {
                            snackbarHostState.showSnackbar(
                                message = context.getString(R.string.csv_saved_at, file.path),
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

                HomeUiAction.ExportReservationsAsCsv -> {
                    val file = state.displayedReservations.exportReservationsToExcel(
                        context = context,
                        isAllRides = true,
                        headers = listOf(
                            context.getString(R.string.date),
                            context.getString(R.string.type),
                            context.getString(R.string.car),
                            context.getString(R.string.client_name),
                            context.getString(R.string.tourism_company),
                            context.getString(R.string.tourism_ride_price),
                            context.getString(R.string.tourism_collected_amount),
                            context.getString(R.string.tourism_balance),
                            context.getString(R.string.travel_company),
                            context.getString(R.string.travel_ride_price),
                            context.getString(R.string.travel_collected_amount),
                            context.getString(R.string.travel_company_balance)
                        )
                    )
                    scope.launch {
                        if (file != null) {
                            snackbarHostState.showSnackbar(
                                message = context.getString(R.string.csv_saved_at, file.path),
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

                is HomeUiAction.OnCopyPhoneNumber -> {
                    clipboardManager.setText(AnnotatedString(action.phoneNumber))
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.number_copied_to_clipboard),
                            withDismissAction = true
                        )
                    }
                }

                is HomeUiAction.OnMessagePhoneNumber -> {
                    PhoneUtil.sendWhatsappMessage(
                        context = context,
                        phoneNumber = action.phoneNumber,
                        message = "",
                        onFailure = {
                            scope.launch {
                                snackbarHostState.showSnackbar(context.getString(R.string.whatsapp_is_not_installed))
                            }
                        }
                    )
                }

                is HomeUiAction.SendConfirmationToClient -> {
                    val reservation =
                        state.displayedReservations.first { it.id == action.reservationId }
                    val messageText =
                        context.getString(
                            R.string.confirmation_message,
                            reservation.clientName,
                            (reservation.date + reservation.time).formatEpochSecondsToMessageDateTime()
                        )
                    PhoneUtil.sendWhatsappMessage(
                        context = context,
                        phoneNumber = reservation.clientPhone,
                        message = messageText,
                        onSuccess = {
                            viewModel.handleAction(HomeUiAction.OnConfirmationSentToClient(action.reservationId))
                        },
                        onFailure = {
                            scope.launch {
                                snackbarHostState.showSnackbar(context.getString(R.string.whatsapp_is_not_installed))
                            }
                        }
                    )
                }

                is HomeUiAction.SendDriverInfoToClient -> {
                    val reservation =
                        state.displayedReservations.first { it.id == action.reservationId }
                    val messageText = context.getString(
                        R.string.reservation_details_message,
                        reservation.clientName,
                        (reservation.date + reservation.time).formatEpochSecondsToMessageDateTime(),
                        reservation.car,
                        reservation.driver,
                        reservation.driverPhoneNumber
                    )
                    PhoneUtil.sendWhatsappMessage(
                        context = context,
                        phoneNumber = reservation.clientPhone,
                        message = messageText,
                        onSuccess = {
                            viewModel.handleAction(HomeUiAction.OnDriverInfoSent(action.reservationId))
                        },
                        onFailure = {
                            scope.launch {
                                snackbarHostState.showSnackbar(context.getString(R.string.whatsapp_is_not_installed))
                            }
                        }
                    )
                }

                is HomeUiAction.SendReservationInfoToTravelCompany -> {
                    val reservation =
                        state.displayedReservations.first { it.id == action.reservationId }
                    val messageText =
                        getTransportationDetailsMessage(
                            context = context,
                            reservation = reservation
                        )
                    PhoneUtil.sendWhatsappMessage(
                        context = context,
                        phoneNumber = reservation.travelCompanyPhone,
                        message = messageText,
                        onSuccess = {
                            viewModel.handleAction(HomeUiAction.OnInfoSentToTravelCompany(action.reservationId))
                        },
                        onFailure = {
                            scope.launch {
                                snackbarHostState.showSnackbar(context.getString(R.string.whatsapp_is_not_installed))
                            }
                        }
                    )
                }

                is HomeUiAction.SendThanksMessageToCustomer -> {
                    val reservation =
                        state.displayedReservations.first { it.id == action.reservationId }
                    val messageText = context.getString(
                        R.string.thanks_message,
                        reservation.clientName.trim()
                    )
                    PhoneUtil.sendWhatsappMessage(
                        context = context,
                        phoneNumber = reservation.clientPhone,
                        message = messageText,
                        onSuccess = {
                            viewModel.handleAction(HomeUiAction.ThanksMessageSent(action.reservationId))
                        },
                        onFailure = {
                            scope.launch {
                                snackbarHostState.showSnackbar(context.getString(R.string.whatsapp_is_not_installed))
                            }
                        }
                    )
                }

                is HomeUiAction.FetchCustomerForUpdating -> {
                    val customer = state.customers.first { it.id == action.customerId }
                    onNavigateToEditCustomer(customer)
                }

                else -> viewModel.handleAction(action)
            }
        },
        context= context
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    reservations: List<Reservation> = emptyList(),
    customers: List<Customer> = emptyList(),
    searchQuery: String = "",
    selectedCountry: String = "",
    selectedTimeFilter: TimeFilter = TimeFilter.All,
    reservationTypes: List<String> = emptyList(),
    cars: List<String> = emptyList(),
    tourismCompanies: List<Company> = emptyList(),
    employees: List<Employee> = emptyList(),
    travelCompanies: List<Company> = emptyList(),
    drivers: List<Employee> = emptyList(),
    countries: List<String> = emptyList(),
    isLoading: Boolean = false,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onAction: (HomeUiAction) -> Unit = {},
    scope: CoroutineScope = rememberCoroutineScope(),
    context: Context = LocalContext.current
) {
    var isOptionsMenuVisible by remember {
        mutableStateOf(false)
    }
    val pagerState = rememberPagerState(pageCount = { 2 })
    var isEditReservationBottomSheetVisible by remember {
        mutableStateOf(false)
    }
    var editedReservation by remember {
        mutableStateOf(Reservation())
    }
    var isConfirmDeleteDialogVisible by remember {
        mutableStateOf(false)
    }
    var selectedItemId by remember {
        mutableStateOf("")
    }
    var isCustomer by remember {
        mutableStateOf(true)
    }
    var isDateRangePickerVisible by remember { mutableStateOf(false) }
    var isFixedDatePickerVisible by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { newValue ->
            newValue != SheetValue.Hidden || !isEditReservationBottomSheetVisible
        }
    )
    val filePicker =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.data?.let { fileUri ->
                importCustomersFromExcel(
                    context = context,
                    fileUri = fileUri
                ) { customers ->
                    onAction(HomeUiAction.AddCustomers(customers))
                }
            }
        }
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(stringResource(R.string.app_name))
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onAction(HomeUiAction.ShowNavDrawer)
                    }) {
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
                                    if (pagerState.currentPage == 1) {
                                        onAction(HomeUiAction.ExportCustomersAsCsv)
                                    } else {
                                        onAction(HomeUiAction.ExportReservationsAsCsv)
                                    }
                                    isOptionsMenuVisible = false
                                },
                                text = {
                                    Text(
                                        text = stringResource(R.string.export_as_csv),
                                    )
                                },
                            )
                            DropdownMenuItem(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                                        type = "text/csv" // MIME type for CSV files
                                        putExtra(
                                            Intent.EXTRA_MIME_TYPES,
                                            arrayOf(
                                                "text/csv",
                                                "application/vnd.ms-excel",
                                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                            )
                                        )
                                    }
                                    filePicker.launch(intent)
                                    isOptionsMenuVisible = false
                                },
                                text = {
                                    Text(
                                        text = stringResource(R.string.import_customers_from_excel),
                                    )
                                },
                            )
                        }
                    }
                }

            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (pagerState.currentPage == 1) {
                        onAction(HomeUiAction.AddCustomer)
                    } else {
                        onAction(HomeUiAction.AddReservation)
                    }
                }
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            AnimatedVisibility(isLoading) {
                LinearProgressIndicator()
            }
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    val data =if(it.matches(Regex("[+\\d\\s]+"))) it.replace(" ","") else it
                    onAction(HomeUiAction.UpdateSearchQuery(data))
                },
                placeholder = { Text(stringResource(R.string.smart_search)) },
                modifier = Modifier
                    .fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                onAction(HomeUiAction.UpdateSearchQuery(""))
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = null
                            )
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.large
            )
            PrimaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                indicator = {
                    TabRowDefaults.PrimaryIndicator(
                        modifier = Modifier
                            .run {
                                if (LocalLayoutDirection.current == LayoutDirection.Rtl)
                                    scale(-1f, 1f)
                                else
                                    this
                            }
                            .tabIndicatorOffset(pagerState.currentPage, true),
                        width = Dp.Unspecified,
                    )
                }
            ) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.reservations),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.customers),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
            HorizontalPager(
                state = pagerState
            ) { page ->
                when (page) {
                    1 -> {
                        Column(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .fillMaxSize()
                        ) {

                            LazyRow {
                                items(countries) { country ->
                                    FilterChip(
                                        modifier = Modifier.padding(end = 8.dp),
                                        onClick = {
                                            onAction(HomeUiAction.UpdateCountryFilter(if (country != selectedCountry) country else ""))
                                        },
                                        label = {
                                            Text(country)
                                        },
                                        selected = selectedCountry == country,
                                        leadingIcon = {
                                            if (selectedCountry == country) {
                                                Icon(
                                                    imageVector = Icons.Filled.Done,
                                                    contentDescription = "Done icon",
                                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                                )
                                            }
                                        },
                                    )
                                }
                            }
                            AnimatedVisibility(selectedCountry!="" && customers.isNotEmpty()) {
                                Text(
                                    text = stringResource(
                                        R.string.customers_count_is_customers,
                                        NumberFormat.getInstance(Locale.getDefault()).format(customers.size)
                                    ),
                                    modifier = Modifier.padding(start = 8.dp,top = 8.dp)
                                )
                            }
                            CustomerListWithTitle(
                                customers = customers,
                                onViewCustomerDetailsClicked = { customerId ->
                                    onAction(
                                        HomeUiAction.OnViewCustomerDetails(customerId)
                                    )
                                },
                                onDeleteCustomer = { customerId ->
                                    isCustomer = true
                                    selectedItemId = customerId
                                    isConfirmDeleteDialogVisible = true
                                },
                                onEditCustomer = { customer ->
                                    onAction(
                                        HomeUiAction.OnEditCustomerClicked(customer)
                                    )
                                },
                                onMessagePhoneNumber = { phoneNumber ->
                                    onAction(HomeUiAction.OnMessagePhoneNumber(phoneNumber))
                                },
                                onCopyPhoneNumber = { phoneNumber ->
                                    onAction(HomeUiAction.OnCopyPhoneNumber(phoneNumber))
                                }
                            )
                        }
                    }

                    0 -> {
                        val totalEarnings = remember(reservations){
                            reservations.sumOf { it.tourismRidePrice - it.travelRidePrice }
                        }
                        Column(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .fillMaxSize()
                        ) {
                            TimeFiltersChips(
                                onUpdateTimeFilter = { timeFilter ->
                                    onAction(
                                        HomeUiAction.UpdateTimeFilter(
                                            if (timeFilter::class == selectedTimeFilter::class) TimeFilter.All else timeFilter
                                        )
                                    )
                                },
                                selectedTimeFilter = selectedTimeFilter,
                                onShowDatePicker = {
                                    isFixedDatePickerVisible = true
                                },
                                onShowDateRangePicker = {
                                    isDateRangePickerVisible = true
                                }
                            )
                            if (selectedTimeFilter is TimeFilter.FixedRange) {
                                Text(
                                    text = stringResource(
                                        R.string.selected_range_place,
                                        selectedTimeFilter.startDate.formatEpochSecondsToMonthlyDate(),
                                        selectedTimeFilter.endDate.formatEpochSecondsToMonthlyDate()
                                    ),
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            } else if(selectedTimeFilter !is TimeFilter.TodayOnwards && selectedTimeFilter !is TimeFilter.All){
                                Text(
                                    text = stringResource(
                                        R.string.selected_date_place,
                                        selectedTimeFilter.getDate()
                                    ),
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            if(selectedTimeFilter !is TimeFilter.All){
                                Text(
                                    text  = stringResource(
                                        R.string.reservation_count_total_earnings,
                                        NumberFormat.getInstance(Locale.getDefault()).format(reservations.size),
                                        context.getString(R.string.sar, NumberFormat.getInstance(Locale.getDefault()).format(totalEarnings))
                                    ),

                                )
                            }

                            ReservationsList(
                                reservations = reservations,
                                onAddReservation = {},
                                isEditProfileEnabled = true,
                                onEditProfile = {
                                    onAction(HomeUiAction.FetchCustomerForUpdating(it))
                                },
                                isHeaderVisible = false,
                                isAddEnabled = false,
                                isSendActionsVisible = true,
                                onDeleteReservation = { reservationId ->
                                    isCustomer = false
                                    selectedItemId = reservationId
                                    isConfirmDeleteDialogVisible = true
                                },
                                onArchiveReservation = { reservationId ->
                                    onAction(HomeUiAction.ArchiveReservation(reservationId))
                                },
                                onCopyPhoneNumber = { phoneNumber ->
                                    onAction(HomeUiAction.OnCopyPhoneNumber(phoneNumber))
                                },
                                onMessagePhoneNumber = { phoneNumber ->
                                    onAction(HomeUiAction.OnMessagePhoneNumber(phoneNumber))
                                },
                                onEditReservation = { reservation ->
                                    editedReservation = reservation
                                    isEditReservationBottomSheetVisible = true
                                },
                                onSendDriverInfoToClient = { reservationId: String ->
                                    onAction(HomeUiAction.SendDriverInfoToClient(reservationId))
                                },
                                onSendInfoToTravelCompany = { reservationId: String ->
                                    onAction(
                                        HomeUiAction.SendReservationInfoToTravelCompany(
                                            reservationId
                                        )
                                    )
                                },
                                onSendConfirmationToCustomer = { reservationId: String ->
                                    onAction(HomeUiAction.SendConfirmationToClient(reservationId))
                                },
                                onSendThanksMessageToCustomer =  {
                                    onAction(HomeUiAction.SendThanksMessageToCustomer(it))
                                }
                            )
                        }
                    }
                }
            }
            AnimatedVisibility(isEditReservationBottomSheetVisible) {
                ModalBottomSheet(
                    sheetState = bottomSheetState,
                    modifier = Modifier.fillMaxSize(),
                    onDismissRequest = {},
                    properties = ModalBottomSheetProperties(shouldDismissOnBackPress = false)

                ) {
                    AddReservationBottomSheetContent(
                        modifier = Modifier.fillMaxSize(),
                        types = reservationTypes,
                        cars = cars,
                        tourismCompanies = tourismCompanies,
                        employees = employees,
                        onFetchEmployees = {
                            onAction(
                                HomeUiAction.FetchEmployees(it)
                            )
                        },
                        travelCompanies = travelCompanies,
                        drivers = drivers,
                        initialReservation = editedReservation,
                        onFetchDrivers = {
                            onAction(
                                HomeUiAction.FetchDrivers(it)
                            )
                        },
                        onSaveReservation = {
                            isEditReservationBottomSheetVisible = false
                            onAction(
                                HomeUiAction.UpdateReservation(
                                    reservation = it,
                                    onSuccess = {
                                        snackbarHostState.showSnackbarWithDuration(
                                            message = context.getString(R.string.reservation_updated_successfully),
                                            durationMillis = 1500L,
                                            scope = scope,
                                            onFinished = {
                                                editedReservation = Reservation()
                                            }
                                        )
                                    }
                                ))
                        },
                        onDismiss = {
                            isEditReservationBottomSheetVisible = false
                            editedReservation = Reservation()
                        }

                    )
                }
            }
            AnimatedVisibility(isDateRangePickerVisible) {
                DateRangePickerModal(
                    onDateRangeSelected = {
                        onAction(
                            HomeUiAction.UpdateTimeFilter(
                                TimeFilter.FixedRange(
                                    it.first?.toSeconds() ?: 0L,
                                    it.second?.toSeconds() ?: 0L
                                )
                            )
                        )
                    },
                    onDismiss = { isDateRangePickerVisible = false }
                )
            }
            AnimatedVisibility(isFixedDatePickerVisible) {
                DateFixedPickerModal(
                    onDateSelected = {
                        onAction(
                            HomeUiAction.UpdateTimeFilter(
                                TimeFilter.FixedDate(it ?: 0L)
                            )
                        )
                    },
                    onDismiss = { isFixedDatePickerVisible = false }
                )
            }
            AnimatedVisibility(isConfirmDeleteDialogVisible) {
                ModalBottomSheet(
                    onDismissRequest = {
                        isConfirmDeleteDialogVisible = false
                        selectedItemId = ""
                    },
                ) {
                    ConfirmDeleteDialog(
                        label = stringResource(id = R.string.reservation),
                        onDismiss = {
                            isConfirmDeleteDialogVisible = false
                            selectedItemId = ""
                        },
                        onConfirm = {
                            isConfirmDeleteDialogVisible = false

                            onAction(
                                if (isCustomer)
                                    HomeUiAction.OnDeleteCustomer(
                                        customerId = selectedItemId,
                                        onShowMessage = { isDeleted ->
                                            snackbarHostState.showSnackbarWithDuration(
                                                message = if (isDeleted) context.getString(R.string.customer_deleted_successfully) else context.getString(R.string.customer_has_reservations_and_cannot_be_deleted),
                                                durationMillis = 1500L,
                                                scope = scope
                                            )
                                        }
                                    )
                                else
                                    HomeUiAction.OnDeleteReservation(selectedItemId)
                            )
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
        HomeScreenContent()
    }
}
