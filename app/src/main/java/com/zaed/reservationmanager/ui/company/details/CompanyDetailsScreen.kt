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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.CompanyBalance
import com.zaed.reservationmanager.data.model.CompanyType
import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.ui.company.details.components.BalanceSection
import com.zaed.reservationmanager.ui.company.details.components.CompanyDetailsHeader
import com.zaed.reservationmanager.ui.company.display.components.ConfirmDeleteDialog
import com.zaed.reservationmanager.ui.home.component.AddReservationBottomSheetContent
import com.zaed.reservationmanager.ui.home.component.ReservationsList
import com.zaed.reservationmanager.ui.util.PhoneUtil
import com.zaed.reservationmanager.ui.util.SheetUtil.exportReservationsAsCSV
import com.zaed.reservationmanager.ui.util.formatEpochSecondsToDateTime
import com.zaed.reservationmanager.ui.util.formatEpochSecondsToMessageDateTime
import com.zaed.reservationmanager.ui.util.formatMoney
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
                            context.getString(R.string.type),
                            context.getString(R.string.car),
                            context.getString(R.string.client_name),
                            context.getString(R.string.selling_price),
                            context.getString(R.string.buying_price),
                            context.getString(R.string.collected_price),
                            context.getString(R.string.balance),
                        )
                    }
                    val file = state.reservations.exportReservationsAsCSV(
                        context = context,
                        headers = headers,
                        isTravelCompany = companyType == CompanyType.TRAVEL,
                        isTourismCompany = companyType == CompanyType.TOURISM,
                        isAllRides = companyType == CompanyType.TRAVEL_AND_TOURISM
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
                        reservation.date.formatEpochSecondsToMessageDateTime(),
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
                        context.getString(R.string.confirmation_message, reservation.clientName, reservation.date.formatEpochSecondsToMessageDateTime())
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
                    val messageText = context.getString(
                        R.string.transportation_details,
                        reservation.clientName,
                        reservation.clientPhone,
                        reservation.date.formatEpochSecondsToMessageDateTime(),
                        reservation.car,
                        reservation.startLocation,
                        reservation.endLocation,
                        reservation.buyingPrice.toInt().toString(),
                        reservation.collectedAmount.toInt().toString(),
                        reservation.note
                    )
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
        balance = state.balance,
        reservations = state.reservations,
        snackBarHostState = snackbarHostState,
        scope = scope,
        context = context
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyDetailsScreenContent(
    modifier: Modifier = Modifier,
    onAction: (CompanyDetailsUiAction) -> Unit,
    company: Company = Company(),
    reservationTypes: List<String> = emptyList(),
    cars: List<String> = emptyList(),
    tourismCompanies: List<Company> = emptyList(),
    employees: List<Employee> = emptyList(),
    travelCompanies: List<Company> = emptyList(),
    drivers: List<Employee> = emptyList(),
    snackBarHostState: SnackbarHostState,
    reservations: List<Reservation> = emptyList(),
    balance: CompanyBalance = CompanyBalance(),
    scope: CoroutineScope = rememberCoroutineScope(),
    context: Context = LocalContext.current
) {
    var isConfirmDeleteDialogVisible by remember {
        mutableStateOf(false)
    }
    var isEditReservationBottomSheetVisible by remember {
        mutableStateOf(false)
    }
    var selectedReservation by remember {
        mutableStateOf(Reservation())
    }
    var isOptionsMenuVisible by remember {
        mutableStateOf(false)
    }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
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
                balance = balance
            )
            ReservationsList(
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                reservations = reservations,
                isEditProfileEnabled = true,
                onEditProfile = {
                    onAction(CompanyDetailsUiAction.FetchCustomerForUpdating(it))
                },
                isAddEnabled = false,
                onArchiveReservation = { reservationId ->
                    onAction(CompanyDetailsUiAction.ArchiveReservation(reservationId))
                },
                onDeleteReservation = {
                    selectedReservation = Reservation(id = it)
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
                }
            )
            AnimatedVisibility(isEditReservationBottomSheetVisible) {
                ModalBottomSheet(
                    onDismissRequest = {
                        isEditReservationBottomSheetVisible = false
                        selectedReservation = Reservation()
                    },
                    sheetState = bottomSheetState,
                    modifier = Modifier.fillMaxSize()
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
                            )
                        },
                        onDismiss = {
                            isEditReservationBottomSheetVisible = false
                            selectedReservation = Reservation()
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
                    sheetState = rememberModalBottomSheetState()
                ) {
                    ConfirmDeleteDialog(
                        label = stringResource(
                            id = R.string.reservation
                        ),
                        onDismiss = {
                            isConfirmDeleteDialogVisible = false
                            selectedReservation = Reservation()
                        },
                        onConfirm = {
                            onAction(
                                CompanyDetailsUiAction.OnDeleteReservation(selectedReservation.id)
                            )
                            isConfirmDeleteDialogVisible = false
                            selectedReservation = Reservation()
                        }
                    )
                }
            }
        }
    }
}