package com.zaed.reservationmanager.ui.company.details

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.CompanyBalance
import com.zaed.reservationmanager.data.model.CompanyPayment
import com.zaed.reservationmanager.data.model.CompanyType
import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.ui.company.details.components.AddPaymentBottomSheetContent
import com.zaed.reservationmanager.ui.company.details.components.BalanceSection
import com.zaed.reservationmanager.ui.company.details.components.CompanyDetailsHeader
import com.zaed.reservationmanager.ui.company.details.components.PaymentsList
import com.zaed.reservationmanager.ui.company.display.components.ConfirmDeleteDialog
import com.zaed.reservationmanager.ui.home.component.AddReservationBottomSheetContent
import com.zaed.reservationmanager.ui.home.component.ReservationsList
import com.zaed.reservationmanager.ui.home.component.getTransportationDetailsMessage
import com.zaed.reservationmanager.ui.util.PhoneUtil
import com.zaed.reservationmanager.ui.util.SheetUtil.exportReservationsToExcel
import com.zaed.reservationmanager.ui.util.formatEpochSecondsToMessageDateTime
import com.zaed.reservationmanager.ui.util.showSnackbarWithDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun CompanyDetailsScreen(
    modifier: Modifier = Modifier,
    viewModel: CompanyDetailsViewModel = koinViewModel(),
    companyId: String,
    companyType: CompanyType = CompanyType.TOURISM,
    onNavigateBack: () -> Unit,
    onNavigateToCompanyDetails: (companyId: String, companyType: CompanyType) -> Unit,
    onNavigateToEditCustomer: (customer: Customer) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    LaunchedEffect(true) {
        viewModel.init(companyId, companyType)
    }
    CompanyDetailsScreenContent(
        modifier = modifier,
        onAction = { action ->
            when (action) {
                CompanyDetailsUiAction.OnBackPressed -> onNavigateBack()
                is CompanyDetailsUiAction.ExportReservationsAsCSV -> {
                    val headers = if (companyType == CompanyType.TRAVEL) {
                        listOf(
                            context.getString(R.string.date),
                            context.getString(R.string.type),
                            context.getString(R.string.car),
                            context.getString(R.string.client_name),
                            context.getString(R.string.buying_price),
                            context.getString(R.string.collected_price),
                            context.getString(R.string.balance),
                        )
                    } else if (companyType == CompanyType.TOURISM) {
                        listOf(
                            context.getString(R.string.date),
                            context.getString(R.string.type),
                            context.getString(R.string.car),
                            context.getString(R.string.client_name),
                            context.getString(R.string.selling_price),
                            context.getString(R.string.collected_price),
                            context.getString(R.string.balance),
                        )
                    } else {
                        listOf(
                            context.getString(R.string.date),
                            context.getString(R.string.time),
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
                    }
                    val file = state.reservations.exportReservationsToExcel(
                        context = context,
                        headers = headers,
                        isTravelCompany = companyType == CompanyType.TRAVEL,
                        isTourismCompany = companyType == CompanyType.TOURISM,
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

                is CompanyDetailsUiAction.OnCompanyClicked -> {
                    onNavigateToCompanyDetails(action.companyId, action.type)
                }

                is CompanyDetailsUiAction.OnCopyPhoneNumber -> {
                    clipboardManager.setText(AnnotatedString(action.phoneNumber))
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.number_copied_to_clipboard),
                            withDismissAction = true
                        )
                    }
                }

                is CompanyDetailsUiAction.OnMessagePhoneNumber -> {
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

                is CompanyDetailsUiAction.SendReservationInfo -> {
                    val reservation = state.reservations.first { it.id == action.reservationId }
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
                            viewModel.handleAction(CompanyDetailsUiAction.ReservationInfoSent(action.reservationId))
                        },
                        onFailure = {
                            scope.launch {
                                snackbarHostState.showSnackbar(context.getString(R.string.whatsapp_is_not_installed))
                            }
                        }
                    )
                }

                is CompanyDetailsUiAction.SendReservationConfirmation -> {
                    val reservation = state.reservations.first { it.id == action.reservationId }
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
                            viewModel.handleAction(
                                CompanyDetailsUiAction.ReservationConfirmationSent(
                                    action.reservationId
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

                is CompanyDetailsUiAction.SendReservationInfoToTravelCompany -> {
                    val reservation = state.reservations.first { it.id == action.reservationId }
                    val messageText = getTransportationDetailsMessage(context,reservation)
                    PhoneUtil.sendWhatsappMessage(
                        context = context,
                        phoneNumber = reservation.travelCompanyPhone,
                        message = messageText,
                        onSuccess = {
                            viewModel.handleAction(
                                CompanyDetailsUiAction.ReservationInfoToTravelCompanySent(
                                    action.reservationId
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

                is CompanyDetailsUiAction.SendThanksMessageToCustomer -> {
                    val reservation = state.reservations.first { it.id == action.reservationId }
                    val messageText = context.getString(
                        R.string.thanks_message,
                        reservation.clientName.trim()
                    )
                    PhoneUtil.sendWhatsappMessage(
                        context = context,
                        phoneNumber = reservation.clientPhone,
                        message = messageText,
                        onSuccess = {
                            viewModel.handleAction(
                                CompanyDetailsUiAction.ThanksMessageSent(
                                    action.reservationId
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

                is CompanyDetailsUiAction.FetchCustomerForUpdating -> {
                    val updatedAction = action.copy(
                        onSuccess = { customer ->
                            onNavigateToEditCustomer(customer)
                        }
                    )
                    viewModel.handleAction(updatedAction)
                }


                else -> viewModel.handleAction(action)
            }
        },
        company = state.company,
        reservations = state.reservations,
        snackBarHostState = snackbarHostState,
        scope = scope,
        context = context,
        balance = state.balance,
        reservationTypes = state.reservationTypes,
        cars = state.cars,
        tourismCompanies = state.tourismCompanies,
        employees = state.employees,
        travelCompanies = state.travelCompanies,
        drivers = state.drivers,
        payments = state.payments,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyDetailsScreenContent(
    modifier: Modifier = Modifier,
    onAction: (CompanyDetailsUiAction) -> Unit,
    company: Company = Company(),
    balance: CompanyBalance = CompanyBalance(),
    reservationTypes: List<String> = emptyList(),
    cars: List<String> = emptyList(),
    tourismCompanies: List<Company> = emptyList(),
    employees: List<Employee> = emptyList(),
    travelCompanies: List<Company> = emptyList(),
    drivers: List<Employee> = emptyList(),
    snackBarHostState: SnackbarHostState,
    reservations: List<Reservation> = emptyList(),
    payments: List<CompanyPayment> = emptyList(),
    scope: CoroutineScope = rememberCoroutineScope(),
    context: Context = LocalContext.current
) {
    var isConfirmDeleteDialogVisible by remember {
        mutableStateOf(false)
    }
    var isAddPaymentDialogVisible by remember {
        mutableStateOf(false)
    }
    var selectedPayment by remember{
        mutableStateOf(CompanyPayment())
    }
    var isEditReservationBottomSheetVisible by remember {
        mutableStateOf(false)
    }
    var selectedReservation by remember {
        mutableStateOf(Reservation())
    }
    var isReservation by remember {
        mutableStateOf(true)
    }
    val pagerState = rememberPagerState(pageCount = { 2 })
    var isOptionsMenuVisible by remember {
        mutableStateOf(false)
    }
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { newValue ->
            newValue != SheetValue.Hidden || !isEditReservationBottomSheetVisible
        }
    )
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.company_details),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onAction(CompanyDetailsUiAction.OnBackPressed) }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBackIos,
                            contentDescription = null
                        )
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
                                    onAction(CompanyDetailsUiAction.ExportReservationsAsCSV)
                                    isOptionsMenuVisible = false
                                },
                                text = {
                                    Text(
                                        text = stringResource(R.string.export_as_csv),
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
                    isAddPaymentDialogVisible = true
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            CompanyDetailsHeader(
                company = company,
                onCopyPhone = { onAction(CompanyDetailsUiAction.OnCopyPhoneNumber(it)) },
                onMessagePhone = { onAction(CompanyDetailsUiAction.OnMessagePhoneNumber(it)) }
            )
            BalanceSection(
                modifier = Modifier.padding(top = 16.dp),
                balance = balance,
                companyType = company.type
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
                            text = stringResource(R.string.payments),
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
                    0 -> {
                        ReservationsList(
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                            reservations = reservations,
                            isHeaderVisible = false,
                            isEditProfileEnabled = true,
                            onEditProfile = {
                                onAction(CompanyDetailsUiAction.FetchCustomerForUpdating(it))
                            },
                            isAddEnabled = false,
                            onArchiveReservation = { reservationId ->
                                onAction(CompanyDetailsUiAction.ArchiveReservation(reservationId))
                            },
                            onDeleteReservation = { reservation ->
                                selectedReservation = reservation
                                isReservation = true
                                isConfirmDeleteDialogVisible = true
                            },
                            onCopyPhoneNumber = { onAction(CompanyDetailsUiAction.OnCopyPhoneNumber(it)) },
                            onMessagePhoneNumber = {
                                onAction(
                                    CompanyDetailsUiAction.OnMessagePhoneNumber(
                                        it
                                    )
                                )
                            },
                            onEditReservation = {
                                selectedReservation = it
                                isEditReservationBottomSheetVisible = true
                            },
                            onSendConfirmationToCustomer = {
                                onAction(
                                    CompanyDetailsUiAction.SendReservationConfirmation(it)
                                )
                            },
                            onSendInfoToTravelCompany = {
                                onAction(
                                    CompanyDetailsUiAction.SendReservationInfoToTravelCompany(it)
                                )
                            },
                            onSendDriverInfoToClient = {
                                onAction(
                                    CompanyDetailsUiAction.SendReservationInfo(it)
                                )
                            },
                            onSendThanksMessageToCustomer = {
                                onAction(
                                    CompanyDetailsUiAction.SendThanksMessageToCustomer(it)
                                )
                            },
                            onAddSecondaryReservation = { mainReservation ->
                                selectedReservation =
                                    Reservation(
                                        reservationNumber = mainReservation.reservationNumber,
                                        mainReservation = false,
                                        mainReservationId = mainReservation.id,
                                        tourismCompany = mainReservation.tourismCompany,
                                        tourismCompanyId = mainReservation.tourismCompanyId,
                                        tourismCompanyPhone = mainReservation.tourismCompanyPhone,
                                        tourismEmployeeId = mainReservation.tourismEmployeeId,
                                        tourismEmployee = mainReservation.tourismEmployee,
                                        tourismEmployeePhone = mainReservation.tourismEmployeePhone,
                                        clientId = mainReservation.clientId,
                                        clientName = mainReservation.clientName,
                                        clientPhone = mainReservation.clientPhone,
                                        clientCountry = mainReservation.clientCountry,
                                    )
                                isEditReservationBottomSheetVisible = true
                            }
                        )
                    }

                    1 -> {
                        PaymentsList(
                            payments = payments,
                            onEditPayment = {
                                selectedPayment = it
                                isAddPaymentDialogVisible = true
                            },
                            onDeletePayment = {
                                selectedPayment = it
                                isReservation = false
                                isConfirmDeleteDialogVisible = true
                            }
                        )
                    }
                }
            }
            AnimatedVisibility(isEditReservationBottomSheetVisible) {
                ModalBottomSheet(
                    onDismissRequest = {},
                    sheetState = bottomSheetState,
                    modifier = Modifier.fillMaxSize(),
                    properties = ModalBottomSheetProperties(shouldDismissOnBackPress = false)

                ) {
                    AddReservationBottomSheetContent(
                        modifier = Modifier.fillMaxSize(),
                        types = reservationTypes,
                        cars = cars,
                        tourismCompanies = tourismCompanies,
                        onFetchEmployees = {
                            onAction(CompanyDetailsUiAction.OnFetchEmployees(it))
                        },
                        employees = employees,
                        travelCompanies = travelCompanies,
                        onFetchDrivers = {
                            onAction(CompanyDetailsUiAction.OnFetchDrivers(it))
                        },
                        drivers = drivers,
                        initialReservation = selectedReservation,
                        onSaveReservation = {
                            isEditReservationBottomSheetVisible = false
                            onAction(
                                if(selectedReservation.id.isNotBlank()){
                                    CompanyDetailsUiAction.OnEditReservation(
                                        reservation = it,
                                        onSuccess = {
                                            snackBarHostState.showSnackbarWithDuration(
                                                message = context.getString(R.string.reservation_updated_successfully),
                                                durationMillis = 1500L,
                                                scope = scope,
                                                onFinished = {
                                                    selectedReservation = Reservation()
                                                }
                                            )
                                        }
                                    )
                                } else {
                                    CompanyDetailsUiAction.OnAddReservation(
                                        reservation = it,
                                        onSuccess = {
                                            snackBarHostState.showSnackbarWithDuration(
                                                message = context.getString(R.string.reservation_added_successfully),
                                                durationMillis = 1500L,
                                                scope = scope,
                                                onFinished = {
                                                    selectedReservation = Reservation()
                                                }
                                            )
                                        }
                                    )
                                }
                            )
                        },
                        onDismiss = {
                            isEditReservationBottomSheetVisible = false
                            selectedReservation = Reservation()
                        }

                    )
                }
            }
            AnimatedVisibility(isAddPaymentDialogVisible) {
                ModalBottomSheet(
                    onDismissRequest = {
                        isAddPaymentDialogVisible = false
                        selectedPayment = CompanyPayment()
                    }
                ) {
                    AddPaymentBottomSheetContent(
                        modifier = Modifier.fillMaxWidth(),
                        initialPayment = selectedPayment,
                        onSavePayment = { payment ->
                            isAddPaymentDialogVisible = false
                            onAction(
                                if(payment.id.isBlank()){
                                    CompanyDetailsUiAction.OnAddPayment(
                                        payment = payment,
                                        onSuccess = {
                                            snackBarHostState.showSnackbarWithDuration(
                                                message = context.getString(R.string.payment_added_successfully),
                                                durationMillis = 1500L,
                                                scope = scope,
                                                onFinished = {
                                                    selectedPayment = CompanyPayment()
                                                }
                                            )
                                        }
                                    )
                                } else {
                                    CompanyDetailsUiAction.OnUpdatePayment(
                                        payment = payment,
                                        onSuccess = {
                                            snackBarHostState.showSnackbarWithDuration(
                                                message = context.getString(R.string.payment_updated_successfully),
                                                durationMillis = 1500L,
                                                scope = scope,
                                                onFinished = {
                                                    selectedPayment = CompanyPayment()
                                                }
                                            )
                                        }
                                    )
                                }
                            )
                        },
                        onDismiss = {
                            isAddPaymentDialogVisible = false
                            selectedPayment = CompanyPayment()
                        }
                    )
                }
            }
            AnimatedVisibility(isConfirmDeleteDialogVisible) {
                ModalBottomSheet(
                    onDismissRequest = {
                        isConfirmDeleteDialogVisible = false
                        selectedReservation = Reservation()
                    },
                ) {
                    ConfirmDeleteDialog(
                        label = stringResource(
                            id = if(isReservation) R.string.reservation else R.string.payment
                        ),
                        onDismiss = {
                            isConfirmDeleteDialogVisible = false
                            selectedReservation = Reservation()
                            selectedPayment = CompanyPayment()
                        },
                        onConfirm = {
                            isConfirmDeleteDialogVisible = false
                            onAction(
                                if(isReservation){
                                    CompanyDetailsUiAction.OnDeleteReservation(selectedReservation)
                                } else {
                                    CompanyDetailsUiAction.OnDeletePayment(
                                        paymentId = selectedPayment.id,
                                        onSuccess = {
                                            snackBarHostState.showSnackbarWithDuration(
                                                message = context.getString(R.string.payment_deleted_successfully),
                                                durationMillis = 1500L,
                                                scope = scope,
                                                onFinished = {
                                                    selectedPayment = CompanyPayment()
                                                }
                                            )
                                        }
                                    )
                                }
                            )
                            selectedReservation = Reservation()
                        }
                    )
                }
            }
        }
    }
}