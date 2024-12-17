package com.zaed.reservationmanager.ui.reservation.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.data.model.Ride
import com.zaed.reservationmanager.ui.reservation.create.component.AddNewReservation
import com.zaed.reservationmanager.ui.reservation.create.component.AddRideBottomSheet
import com.zaed.reservationmanager.ui.reservation.create.component.CenterAlignedTopBar
import com.zaed.reservationmanager.ui.reservation.create.component.EnteredRidesSection
import com.zaed.reservationmanager.ui.reservation.create.component.MainActionButtons
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme
import com.zaed.reservationmanager.ui.util.showSnackbarWithDuration
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun CreateReservationScreen(
    reservation: Reservation = Reservation(),
    initialCustomer: Customer = Customer(),
    viewModel: CreateReservationViewModel = koinViewModel(),
    navigateBack: () -> Unit
) {
    LaunchedEffect(true) {
        viewModel.init(reservation, initialCustomer)
    }

    val state by viewModel.state.collectAsState()

    CreateReservationScreenContent(
        reservation = state.reservation,
        successStatus = state.successStatus,
        initialReservation = reservation,
        travelCompanies = state.travelCompanies,
        tourismCompanies = state.tourismCompanies,
        rides = state.rides,
        newRide = state.newRide,
        isEditMode = reservation.id.isNotBlank(),
        types = state.transactionTypes,
        cars = state.carTypes,
        drivers = state.drivers,
        action = viewModel::handleAction,
        userMessage = state.userMessage,
        employees = state.employees,
        countries = state.countries,
        isLoading = state.loading,
        rideError = state.rideError,
        reservationError = state.reservationError,
        onBackClicked = navigateBack,
        resetSuccessStatus = {
            viewModel.resetSuccessStatus()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReservationScreenContent(
    reservation: Reservation = Reservation(),
    travelCompanies: List<Company> = emptyList(),
    tourismCompanies: List<Company> = emptyList(),
    types: List<String> = emptyList(),
    cars: List<String> = emptyList(),
    successStatus: Boolean = false,
    isEditMode: Boolean = false,
    drivers: List<Employee> = emptyList(),
    rides: List<Ride> = emptyList(),
    newRide: Ride = Ride(),
    initialReservation: Reservation = Reservation(),
    employees: List<Employee> = emptyList(),
    countries: List<String> = emptyList(),
    action: (ReservationUiAction) -> Unit = {},
    isLoading: Boolean = false,
    rideError: ReservationError = ReservationError.NONE,
    reservationError: ReservationError = ReservationError.NONE,
    userMessage: String = "",
    onBackClicked: () -> Unit = {},
    resetSuccessStatus: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var isAddMovementSheetVisible by remember { mutableStateOf(false) }
    val addMovementSheetState = rememberModalBottomSheetState(true)
    if (userMessage.isNotBlank()) {
        isAddMovementSheetVisible = false
        LaunchedEffect(true) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = userMessage
                )
            }
        }
    }
    LaunchedEffect(successStatus) {
        if (successStatus) {
            if (isAddMovementSheetVisible) {
                isAddMovementSheetVisible = false
                resetSuccessStatus()
            } else {
                snackbarHostState.showSnackbarWithDuration(
                    message = context.getString(
                        if (isEditMode)
                            R.string.reservation_updated_successfully
                        else
                            R.string.reservation_added_successfully
                    ),
                    durationMillis = 1500L,
                    scope = scope,
                    onFinished = {
                        onBackClicked()
                    }
                )
            }
        }
    }
    if (reservationError != ReservationError.NONE) {
        isAddMovementSheetVisible = false
    }
    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        topBar = {
            CenterAlignedTopBar(
                onBackClicked = onBackClicked,
                title = stringResource(R.string.create_new_reservation)
            )
        },
        bottomBar = {
            MainActionButtons(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                isEditMode = isEditMode,
                action = action
            ) {
                onBackClicked()
            }
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            if (isLoading) {
                LinearProgressIndicator()
            }
            AddNewReservation(
                initialReservation = initialReservation,
                reservation,
                action,
                reservationError,
                countries,
                tourismCompanies,
                employees
            )
            EnteredRidesSection(
                isEditMode = isEditMode,
                rides = rides,
                onAddMovementClicked = {
                    action(ReservationUiAction.ValidateReservationData)
                    if (reservationError != ReservationError.NONE) return@EnteredRidesSection
                    isAddMovementSheetVisible = true
                },
                onEditRide = { ride ->
                    action(ReservationUiAction.EditRide(ride))
                    isAddMovementSheetVisible = true
                },
                onDeleteRide = {
                    action(ReservationUiAction.DeleteRide(it))
                }
            )

        }
        if (isAddMovementSheetVisible) {
            AddRideBottomSheet(
                addMovementSheetState,
                rideError,
                action,
                newRide,
                types,
                cars,
                travelCompanies,
                drivers,
                onDismissRequest = { isAddMovementSheetVisible = false }
            )
        }
    }
}


@Preview(device = "spec:id=reference_phone,shape=Normal,width=411,height=891,unit=dp,dpi=420")
@Composable
fun NewClientDataEntryScreenPreview() {
    ReservationManagerTheme {
        CreateReservationScreenContent(
            rides = listOf(
                Ride(
                )
            )
        )
    }
}