package com.zaed.reservationmanager.ui.client.details

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.CompanyType
import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.ui.client.details.components.CustomerDetailsHeader
import com.zaed.reservationmanager.ui.company.display.components.ConfirmDeleteDialog
import com.zaed.reservationmanager.ui.home.component.AddReservationBottomSheetContent
import com.zaed.reservationmanager.ui.home.component.ReservationsList
import com.zaed.reservationmanager.ui.util.PhoneUtil
import com.zaed.reservationmanager.ui.util.formatEpochSecondsToMessageDateTime
import com.zaed.reservationmanager.ui.util.showSnackbarWithDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun CustomerDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: CustomerDetailsViewModel = koinViewModel(),
    customerId: String = "",
    onBackPressed: () -> Unit = {},
    onNavigateToCompanyDetails: (companyId: String, companyType: CompanyType) -> Unit = { _, _ -> },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    LaunchedEffect(true) {
        viewModel.init(customerId)
    }
    CustomerDetailScreenContent(
        modifier = modifier,
        customer = state.customer,
        reservations = state.reservations,
        reservationTypes = state.reservationTypes,
        cars = state.cars,
        tourismCompanies = state.tourismCompanies,
        employees = state.employees,
        travelCompanies = state.travelCompanies,
        drivers = state.drivers,
        snackbarHostState = snackbarHostState,
        onAction = { action ->
            when (action) {
                CustomerDetailsUiAction.OnBackPressed -> {
                    onBackPressed()
                }

                is CustomerDetailsUiAction.OnCompanyClicked -> {
                    onNavigateToCompanyDetails(action.companyId, CompanyType.TRAVEL)
                }

                is CustomerDetailsUiAction.OnCopyPhone -> {
                    clipboardManager.setText(AnnotatedString(action.phoneNumber))
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.number_copied_to_clipboard),
                            withDismissAction = true
                        )
                    }
                }

                is CustomerDetailsUiAction.OnMessagePhone -> {
                    if (action.phoneNumber.isNotBlank()) {
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
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar(context.getString(R.string.phonenumber_is_blank))
                        }
                    }
                }

                is CustomerDetailsUiAction.SendReservationInfo -> {
                    val reservation = state.reservations.first { it.id == action.reservationId }
                    val messageText =

                        context.getString(
                            R.string.reservation_details_message,
                            reservation.clientName,
                            (reservation.date + reservation.time).formatEpochSecondsToMessageDateTime(),
                            reservation.driver,
                            reservation.driverPhoneNumber
                        )
                    PhoneUtil.sendWhatsappMessage(
                        context = context,
                        phoneNumber = reservation.clientPhone,
                        message = messageText,
                        onSuccess = {
                            viewModel.handleAction(
                                CustomerDetailsUiAction.ReservationInfoSent(
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

                is CustomerDetailsUiAction.SendReservationConfirmation -> {
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
                                CustomerDetailsUiAction.ReservationConfirmationSent(
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

                is CustomerDetailsUiAction.SendReservationInfoToTravelCompany -> {
                    val reservation = state.reservations.first { it.id == action.reservationId }
                    val messageText = context.getString(
                        R.string.transportation_details,
                        reservation.clientName,
                        reservation.clientPhone,
                        (reservation.date+ reservation.time).formatEpochSecondsToMessageDateTime(),
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
                                CustomerDetailsUiAction.ReservationInfoToTravelCompanySent(
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

                else -> viewModel.handleAction(action)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomerDetailScreenContent(
    modifier: Modifier = Modifier,
    customer: Customer = Customer(),
    reservations: List<Reservation> = emptyList(),
    reservationTypes: List<String> = emptyList(),
    cars: List<String> = emptyList(),
    tourismCompanies: List<Company> = emptyList(),
    employees: List<Employee> = emptyList(),
    travelCompanies: List<Company> = emptyList(),
    drivers: List<Employee> = emptyList(),
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
    onAction: (CustomerDetailsUiAction) -> Unit = {},
    context: Context = LocalContext.current,
    scope: CoroutineScope = rememberCoroutineScope()
) {
    var isConfirmDeleteDialogVisible by remember {
        mutableStateOf(false)
    }
    var selectedReservation by remember {
        mutableStateOf(Reservation())
    }
    var isAddReservationBottomSheetVisible by remember {
        mutableStateOf(false)
    }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.customer_details),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onAction(CustomerDetailsUiAction.OnBackPressed) }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBackIos,
                            contentDescription = null
                        )
                    }
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            CustomerDetailsHeader(
                customer = customer,
                onCopyPhone = { onAction(CustomerDetailsUiAction.OnCopyPhone(it)) },
                onMessagePhone = { onAction(CustomerDetailsUiAction.OnMessagePhone(it)) },
            )
            ReservationsList(
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                reservations = reservations,
                isEditProfileEnabled = false,
                onArchiveReservation = { reservationId ->
                    onAction(
                        CustomerDetailsUiAction.ArchiveReservation(reservationId)
                    )
                },
                onDeleteReservation = {
                    selectedReservation = Reservation(id = it)
                    isConfirmDeleteDialogVisible = true
                },
                onCopyPhoneNumber = { onAction(CustomerDetailsUiAction.OnCopyPhone(it)) },
                onMessagePhoneNumber = {
                    onAction(
                        CustomerDetailsUiAction.OnMessagePhone(it)
                    )
                },
                onEditReservation = {
                    selectedReservation = it
                    isAddReservationBottomSheetVisible = true
                },
                onSendConfirmationToCustomer = {
                    onAction(
                        CustomerDetailsUiAction.SendReservationConfirmation(it)
                    )
                },
                onSendInfoToTravelCompany = {
                    onAction(
                        CustomerDetailsUiAction.SendReservationInfoToTravelCompany(it)
                    )
                },
                onSendDriverInfoToClient = {
                    onAction(
                        CustomerDetailsUiAction.SendReservationInfo(it)
                    )
                },
                onAddReservation = {
                    selectedReservation = Reservation()
                    isAddReservationBottomSheetVisible = true
                },

                )
            AnimatedVisibility(isAddReservationBottomSheetVisible) {
                ModalBottomSheet(
                    onDismissRequest = {
                        isAddReservationBottomSheetVisible = false
                        selectedReservation = Reservation()
                    },
                    modifier = Modifier.fillMaxSize(),
                    sheetState = bottomSheetState
                ) {
                    AddReservationBottomSheetContent(
                        modifier = Modifier.fillMaxSize(),
                        types = reservationTypes,
                        cars = cars,
                        tourismCompanies = tourismCompanies,
                        employees = employees,
                        onFetchEmployees = {
                            onAction(
                                CustomerDetailsUiAction.OnFetchEmployees(it)
                            )
                        },
                        travelCompanies = travelCompanies,
                        drivers = drivers,
                        initialReservation = selectedReservation,
                        onFetchDrivers = {
                            onAction(
                                CustomerDetailsUiAction.OnFetchDrivers(it)
                            )
                        },
                        onSaveReservation = { reservation ->
                            isAddReservationBottomSheetVisible = false
                            onAction(
                                if (selectedReservation.id.isEmpty()) {
                                    CustomerDetailsUiAction.OnAddReservation(
                                        reservation,
                                        onSuccess = {
                                            snackbarHostState.showSnackbarWithDuration(
                                                message = context.getString(R.string.reservation_added_successfully),
                                                durationMillis = 1500L,
                                                scope = scope,
                                                onFinished = {
                                                    selectedReservation = Reservation()
                                                }
                                            )
                                        })
                                } else {
                                    CustomerDetailsUiAction.OnUpdateReservation(
                                        reservation,
                                        onSuccess = {
                                            snackbarHostState.showSnackbarWithDuration(
                                                message = context.getString(R.string.reservation_updated_successfully),
                                                durationMillis = 1500L,
                                                scope = scope,
                                                onFinished = {
                                                    selectedReservation = Reservation()
                                                }
                                            )
                                        })
                                }
                            )
                        },
                        onDismiss = {
                            isAddReservationBottomSheetVisible = false
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
                                CustomerDetailsUiAction.OnDeleteReservation(selectedReservation.id)
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