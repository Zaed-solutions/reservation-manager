package com.zaed.reservationmanager.ui.reservation.archive

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.ui.home.HomeUiAction
import com.zaed.reservationmanager.ui.home.component.AddReservationBottomSheetContent
import com.zaed.reservationmanager.ui.home.component.ReservationsList
import com.zaed.reservationmanager.ui.home.component.getClientConfirmationMessage
import com.zaed.reservationmanager.ui.home.component.getDriverInfoMessage
import com.zaed.reservationmanager.ui.home.component.getThanksMessage
import com.zaed.reservationmanager.ui.home.component.getTransportationDetailsMessage
import com.zaed.reservationmanager.ui.util.PhoneUtil
import com.zaed.reservationmanager.ui.util.formatEpochSecondsToMessageDateTime
import com.zaed.reservationmanager.ui.util.showSnackbarWithDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ArchiveScreen(
    modifier: Modifier = Modifier,
    viewModel: ArchiveViewModel = koinViewModel(),
    onShowNavDrawer: () -> Unit,
    onNavigateToCustomerDetails: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    ArchiveScreenContent(
        modifier = modifier,
        reservations = state.reservations,
        onAction = { action ->
            when (action) {
                is ArchiveUiAction.ShowNavDrawer -> {
                    onShowNavDrawer()
                }

                is ArchiveUiAction.CopyPhoneNumber -> {
                    clipboardManager.setText(AnnotatedString(action.phoneNumber))
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.number_copied_to_clipboard),
                            withDismissAction = true
                        )
                    }
                }

                is ArchiveUiAction.SendDriverInfoToClient -> {
                    val reservation =
                        state.reservations.first { it.id == action.reservationId }
                    val messageText = getDriverInfoMessage(context, reservation)
                    PhoneUtil.sendWhatsappMessage(
                        context = context,
                        phoneNumber = reservation.clientPhone,
                        message = messageText,
                        onSuccess = {
                            viewModel.handleAction(ArchiveUiAction.OnDriverInfoSent(action.reservationId))
                        },
                        onFailure = {
                            scope.launch {
                                snackbarHostState.showSnackbar(context.getString(R.string.whatsapp_is_not_installed))
                            }
                        }
                    )
                }

                is ArchiveUiAction.SendConfirmationToClient -> {
                    val reservation =
                        state.reservations.first { it.id == action.reservationId }
                    val messageText = getClientConfirmationMessage(context, reservation)
                    PhoneUtil.sendWhatsappMessage(
                        context = context,
                        phoneNumber = reservation.clientPhone,
                        message = messageText,
                        onSuccess = {
                            viewModel.handleAction(ArchiveUiAction.OnConfirmationSentToClient(action.reservationId))
                        },
                        onFailure = {
                            scope.launch {
                                snackbarHostState.showSnackbar(context.getString(R.string.whatsapp_is_not_installed))
                            }
                        }
                    )
                }

                is ArchiveUiAction.SendReservationInfoToTravelCompany -> {
                    val reservation =
                        state.reservations.first { it.id == action.reservationId }
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
                            viewModel.handleAction(ArchiveUiAction.OnInfoSentToTravelCompany(action.reservationId))
                        },
                        onFailure = {
                            scope.launch {
                                snackbarHostState.showSnackbar(context.getString(R.string.whatsapp_is_not_installed))
                            }
                        }
                    )
                }

                is ArchiveUiAction.SendThanksMessageToCustomer -> {
                    val reservation =
                        state.reservations.first { it.id == action.reservationId }
                    val messageText = getThanksMessage(context, reservation)
                    PhoneUtil.sendWhatsappMessage(
                        context = context,
                        phoneNumber = reservation.clientPhone,
                        message = messageText,
                        onSuccess = {
                            viewModel.handleAction(ArchiveUiAction.ThanksMessageSent(action.reservationId))
                        },
                        onFailure = {
                            scope.launch {
                                snackbarHostState.showSnackbar(context.getString(R.string.whatsapp_is_not_installed))
                            }
                        }
                    )
                }

                is ArchiveUiAction.MessagePhoneNumber -> {
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

                is ArchiveUiAction.OnViewCustomerDetails -> onNavigateToCustomerDetails(action.customerId)

                else -> viewModel.handleAction(action)
            }
        },
        snackBarHostState = snackbarHostState,
        reservationTypes = state.reservationTypes,
        cars = state.cars,
        tourismCompanies = state.tourismCompanies,
        employees = state.employees,
        travelCompanies = state.travelCompanies,
        drivers = state.drivers,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArchiveScreenContent(
    modifier: Modifier = Modifier,
    reservations: List<Reservation>,
    onAction: (ArchiveUiAction) -> Unit,
    reservationTypes: List<String> = emptyList(),
    cars: List<String> = emptyList(),
    tourismCompanies: List<Company> = emptyList(),
    employees: List<Employee> = emptyList(),
    travelCompanies: List<Company> = emptyList(),
    drivers: List<Employee> = emptyList(),
    snackBarHostState: SnackbarHostState,
    scope: CoroutineScope = rememberCoroutineScope(),
    context: Context = LocalContext.current
) {
    var isAddReservationBottomSheetVisible by remember {
        mutableStateOf(false)
    }
    var selectedReservation by remember {
        mutableStateOf(Reservation())
    }
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { newValue ->
            newValue != SheetValue.Hidden || !isAddReservationBottomSheetVisible
        }
    )
    val totalEarnings = remember(reservations) {
        reservations.sumOf { it.tourismRidePrice - it.travelRidePrice }
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.reservation_archive),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onAction(ArchiveUiAction.ShowNavDrawer)
                    }) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = null)
                    }

                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(stringResource(R.string.total_reservations_count))
                    }
                    append(" ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
                        append(
                            NumberFormat.getInstance(Locale.getDefault())
                                .format(reservations.size)
                        )
                    }
                    append(" | ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(stringResource(R.string.total_earnings) + ":")
                    }
                    append(" ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
                        append(
                            NumberFormat.getCurrencyInstance(Locale.getDefault())
                                .apply {
                                    maximumFractionDigits = 0
                                }.format(
                                    totalEarnings
                                )

                        )
                    }
                },
                modifier = Modifier.padding(vertical = 16.dp)
            )
            ReservationsList(
                reservations = reservations,
                isEditProfileEnabled = true,
                onNavigateToProfile = {
                    onAction(ArchiveUiAction.OnViewCustomerDetails(it))
                },
                onArchiveReservation = { reservationId ->
                    onAction(ArchiveUiAction.UnarchiveReservation(reservationId))
                },
                isHeaderVisible = false,
                isAddEnabled = false,
                isSendActionsVisible = true,
                onSendConfirmationToCustomer = {
                    onAction(ArchiveUiAction.SendConfirmationToClient(it))
                },
                onSendThanksMessageToCustomer =  {
                    onAction(ArchiveUiAction.SendThanksMessageToCustomer(it))
                },
                onSendInfoToTravelCompany = {
                    onAction(ArchiveUiAction.SendReservationInfoToTravelCompany(it))
                },
                onSendDriverInfoToClient = {
                    onAction(ArchiveUiAction.SendDriverInfoToClient(it))
                },
                isEditable = true,
                onEditReservation = { reservation ->
                    selectedReservation = reservation
                    isAddReservationBottomSheetVisible = true
                },
                onDeleteReservation = { reservation ->
                    onAction(ArchiveUiAction.DeleteReservation(reservation))
                },
                onCopyPhoneNumber = { phoneNumber ->
                    onAction(ArchiveUiAction.CopyPhoneNumber(phoneNumber))
                },
                onMessagePhoneNumber = { phoneNumber ->
                    onAction(ArchiveUiAction.MessagePhoneNumber(phoneNumber))
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
                    isAddReservationBottomSheetVisible = true
                }
            )

            AnimatedVisibility(isAddReservationBottomSheetVisible) {
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
                            onAction(ArchiveUiAction.OnFetchEmployees(it))
                        },
                        employees = employees,
                        travelCompanies = travelCompanies,
                        onFetchDrivers = {
                            onAction(ArchiveUiAction.OnFetchDrivers(it))
                        },
                        drivers = drivers,
                        initialReservation = selectedReservation,
                        onSaveReservation = {
                            isAddReservationBottomSheetVisible = false
                            onAction(
                                if (selectedReservation.id.isNotBlank()) {
                                    ArchiveUiAction.OnEditReservation(
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
                                    ArchiveUiAction.OnAddReservation(
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
                            isAddReservationBottomSheetVisible = false
                            selectedReservation = Reservation()
                        }

                    )
                }
            }
        }
    }
}