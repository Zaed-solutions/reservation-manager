package com.zaed.reservationmanager.ui.reservation.details

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.data.model.Ride
import com.zaed.reservationmanager.ui.company.display.components.ConfirmDeleteDialog
import com.zaed.reservationmanager.ui.reservation.create.ReservationError
import com.zaed.reservationmanager.ui.reservation.create.ReservationUiAction
import com.zaed.reservationmanager.ui.reservation.create.component.AddRideBottomSheet
import com.zaed.reservationmanager.ui.reservation.details.components.AddRideBottomSheetContent
import com.zaed.reservationmanager.ui.reservation.details.components.ReservationDetailsHeader
import com.zaed.reservationmanager.ui.reservation.details.components.RidesList
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme
import com.zaed.reservationmanager.ui.util.PhoneUtil
import com.zaed.reservationmanager.ui.util.formatEpochSecondsToDateTime
import com.zaed.reservationmanager.ui.util.formatMoney
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun ReservationDetailsScreen(
    modifier: Modifier = Modifier,
    viewModel: ReservationDetailsViewModel = koinViewModel(),
    onBackPressed: () -> Unit = {},
    reservationId: String = "",
    onNavigateToClientDetails: (String) -> Unit = {},
    onNavigateToCompanyDetails: (String, Boolean) -> Unit = {_,_ ->},
    onNavigateToEmployeeDetails: (String, Boolean) -> Unit = {_,_ ->},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    LaunchedEffect (true){
        viewModel.init(reservationId)
    }
    ReservationDetailsScreenContent(
        modifier = modifier,
        reservation = state.reservation,
        rides = state.rides,
        snackbarHostState = snackbarHostState,
        onAction = { action ->
            when(action){
                ReservationDetailsUiAction.OnBackPressed -> {
                    onBackPressed()
                }
                is ReservationDetailsUiAction.OnClientClicked -> {
                    onNavigateToClientDetails(action.clientId)
                }
                is ReservationDetailsUiAction.OnCompanyClicked -> {
                    onNavigateToCompanyDetails(action.companyId, action.isTravel)
                }
                is ReservationDetailsUiAction.OnCopyPhoneNumber -> {
                    clipboardManager.setText(AnnotatedString(action.phoneNumber))
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.phone_number_copied_to_clipboard),
                            withDismissAction = true
                        )
                    }
                }
                is ReservationDetailsUiAction.OnEmployeeClicked -> {
                    onNavigateToEmployeeDetails(action.employeeId, action.isDriver)
                }
                is ReservationDetailsUiAction.OnMessagePhoneNumber -> {
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
                ReservationDetailsUiAction.OnSendConfirmationMessage -> {
                    val messageText =
                        context.getString(R.string.we_have_a_confirmed_travel_booking_for_you_kindly_contact_me_upon_your_safe_arrival)
                    PhoneUtil.sendWhatsappMessage(
                        context = context,
                        phoneNumber = state.reservation.clientPhone,
                        message = messageText,
                        onSuccess = {
                            viewModel.handleAction(ReservationDetailsUiAction.OnConfirmationMessageSent)
                        },
                        onFailure = {
                            scope.launch {
                                snackbarHostState.showSnackbar(context.getString(R.string.whatsapp_is_not_installed))
                            }
                        }
                    )
                }
                is ReservationDetailsUiAction.OnSendDriverInfoToCustomer -> {
                    val messageText = context.getString(
                        R.string.it_is_our_pleasure_to_serve_you_your_driver_can_be_reached_at_wishing_you_a_safe_and_pleasant_journey_god_willing,
                        action.driverName,
                        action.driverPhoneNumber
                    )
                    PhoneUtil.sendWhatsappMessage(
                        context = context,
                        phoneNumber = state.reservation.clientPhone,
                        message = messageText,
                        onSuccess = {
                            viewModel.handleAction(ReservationDetailsUiAction.OnDriverInfoSent(action.rideId))
                        },
                        onFailure = {
                            scope.launch {
                                snackbarHostState.showSnackbar(context.getString(R.string.whatsapp_is_not_installed))
                            }
                        }
                    )
                }
                is ReservationDetailsUiAction.OnSendInfoToTravelCompany -> {
                    val messageText = context.getString(
                        R.string.transportation_details,
                        action.ride.travelCompany,
                        state.reservation.clientName,
                        state.reservation.clientPhone,
                        action.ride.date.formatEpochSecondsToDateTime(),
                        action.ride.startLocation,
                        action.ride.endLocation,
                        state.reservation.flightNumber,
                        action.ride.buyingPrice.formatMoney(),
                        action.ride.collectedPrice.formatMoney()
                    )
                    PhoneUtil.sendWhatsappMessage(
                        context = context,
                        phoneNumber = action.ride.travelCompanyPhone,
                        message = messageText,
                        onSuccess = {
                            viewModel.handleAction(ReservationDetailsUiAction.OnInfoSentToTravelCompany(action.ride.id))
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
        },
        types = state.types,
        cars = state.cars,
        travelCompanies = state.travelCompanies,
        drivers = state.drivers,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReservationDetailsScreenContent(
    modifier: Modifier = Modifier,
    onAction: (ReservationDetailsUiAction) -> Unit = {},
    reservation: Reservation = Reservation(),
    rides: List<Ride> = emptyList(),
    types: List<String> = emptyList(),
    cars: List<String> = emptyList(),
    travelCompanies: List<Company> = emptyList(),
    drivers: List<Employee> = emptyList(),
    snackbarHostState: SnackbarHostState = remember{ SnackbarHostState() }
) {
    var isConfirmDeleteDialogVisible by remember {
        mutableStateOf(false)
    }
    var isAddRideBottomSheetVisible by remember{
        mutableStateOf(false)
    }
    var selectedRideId by remember {
        mutableStateOf("")
    }
    Scaffold (
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.reservation_details),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onAction(ReservationDetailsUiAction.OnBackPressed) }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ){ innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
//                .verticalScroll(rememberScrollState())
        ) {
            ReservationDetailsHeader(
                modifier = Modifier.padding(top = 16.dp),
                reservation = reservation,
                onClientClicked = {
                    onAction(ReservationDetailsUiAction.OnClientClicked(reservation.clientId))
                },
                onCopyPhone = { phoneNumber ->
                    onAction(ReservationDetailsUiAction.OnCopyPhoneNumber(phoneNumber))
                },
                onMessagePhone = { phoneNumber ->
                    onAction(ReservationDetailsUiAction.OnMessagePhoneNumber(phoneNumber))
                },
                onTourismEmployeeClicked = {
                    onAction(ReservationDetailsUiAction.OnEmployeeClicked(reservation.tourismEmployeeId))
                },
                onTourismCompanyClicked = {
                    onAction(ReservationDetailsUiAction.OnCompanyClicked(reservation.tourismCompanyId))
                },
                onSendConfirmationMessage = {
                    onAction(ReservationDetailsUiAction.OnSendConfirmationMessage)
                }
            )
            RidesList(
                modifier = Modifier.padding(top = 16.dp),
                rides = rides,
                onAddRide = {
                    isAddRideBottomSheetVisible = true
                },
                onDriverClicked = { driverId ->
                    onAction(ReservationDetailsUiAction.OnEmployeeClicked(driverId, true))
                },
                onCopyPhoneNumber = { phoneNumber ->
                    onAction(ReservationDetailsUiAction.OnCopyPhoneNumber(phoneNumber))
                },
                onMessagePhoneNumber = { phoneNumber ->
                    onAction(ReservationDetailsUiAction.OnMessagePhoneNumber(phoneNumber))
                },
                onDeleteRide = { rideId ->
                    Log.d("ReservationDetails", "ReservationDetailsScreenContent: onDeleteRide: $rideId")
                    selectedRideId = rideId
                    isConfirmDeleteDialogVisible = true
                },
                onCompanyClicked = { companyId ->
                    onAction(ReservationDetailsUiAction.OnCompanyClicked(companyId, true))
                },
                onSendInfoToTravelCompany = { ride ->
                    onAction(ReservationDetailsUiAction.OnSendInfoToTravelCompany(ride))
                },
                onSendDriverInfoToClient = {rideId, driverName, driverPhoneNumber ->
                    onAction(ReservationDetailsUiAction.OnSendDriverInfoToCustomer(rideId, driverName, driverPhoneNumber))
                },
            )
            AnimatedVisibility(isConfirmDeleteDialogVisible){
                ModalBottomSheet(
                    onDismissRequest = {
                        isConfirmDeleteDialogVisible = false
                        selectedRideId = ""
                    },
                    sheetState = rememberModalBottomSheetState()
                ) {
                    ConfirmDeleteDialog(
                        label = stringResource(id = R.string.ride),
                        onDismiss = {
                            isConfirmDeleteDialogVisible = false
                            selectedRideId = ""
                        },
                        onConfirm = {
                            onAction(ReservationDetailsUiAction.OnDeleteRide(selectedRideId))
                            isConfirmDeleteDialogVisible = false
                            selectedRideId = ""
                        }
                    )
                }
            }
            AnimatedVisibility(visible = isAddRideBottomSheetVisible) {
                ModalBottomSheet(
                    onDismissRequest = {
                        isAddRideBottomSheetVisible = false
                    },
                    sheetState = rememberModalBottomSheetState()
                ) {
                    AddRideBottomSheetContent(
                        types=types,
                        cars = cars,
                        drivers = drivers,
                        travelCompanies = travelCompanies,
                        onFetchDrivers = { companyId ->
                            onAction(ReservationDetailsUiAction.UpdateDrivers(companyId))
                        },
                        onAddRide = { ride ->
                            onAction(ReservationDetailsUiAction.OnAddRide(ride))
                            isAddRideBottomSheetVisible = false
                        },
                        onDismiss = {
                            isAddRideBottomSheetVisible = false
                        }
                    )
                }
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun Preview() {
    ReservationManagerTheme {
        ReservationDetailsScreenContent()
    }
}