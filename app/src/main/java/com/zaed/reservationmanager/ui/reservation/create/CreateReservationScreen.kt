package com.zaed.reservationmanager.ui.reservation.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.data.model.Ride
import com.zaed.reservationmanager.ui.components.TitledDropDownTextField
import com.zaed.reservationmanager.ui.components.TitledTextField
import com.zaed.reservationmanager.ui.reservation.create.component.AddRideBottomSheet
import com.zaed.reservationmanager.ui.reservation.create.component.CenterAlignedTopBar
import com.zaed.reservationmanager.ui.reservation.details.components.RideItem
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun CreateReservationScreen(
    viewModel: CreateReservationViewModel = koinViewModel(),
    navigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state.successStatus) {
        if (state.successStatus) {
            navigateBack()
        }
    }
    CreateReservationScreenContent(
        reservation = state.reservation,
        travelCompanies = state.travelCompanies,
        tourismCompanies = state.tourismCompanies,
        rides = state.rides,
        newRide = state.newRide,
        types = state.transactionTypes,
        cars = state.carTypes,
        drivers = state.drivers,
        action = viewModel::handleAction,
        userMessage = state.userMessage,
        employees = state.employees,
        countries = state.countries,
        isLoading = state.loading,
        errorMessage = state.errorMessage,
        onBackClicked = navigateBack
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
    drivers: List<Employee> = emptyList(),
    rides: List<Ride> = emptyList(),
    newRide: Ride = Ride(),
    employees: List<Employee> = emptyList(),
    countries: List<String> = emptyList(),
    action: (ReservationUiAction) -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: ReservationError = ReservationError.NONE,
    userMessage: String = "",
    onBackClicked: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
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
    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        topBar = {
            CenterAlignedTopBar(
                onBackClicked = onBackClicked,
                title = stringResource(R.string.create_new_reservation)
            )
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


            TitledTextField(
                title = stringResource(R.string.client_phone),
                initialValue = reservation.clientPhone,
                onValueChanged = { newText ->
                    action(
                        ReservationUiAction.UpdateCustomerNumber(
                            newText
                        )
                    )
                },
                isOptional = false,
                isError = errorMessage == ReservationError.CUSTOMER_PHONE_IS_REQUIRED,
                errorMessageRes = errorMessage.messageRes,
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Search,
                keyboardActions = KeyboardActions(
                    onSearch = {
                        action(ReservationUiAction.SearchClient)
                    }
                )
            )
            TitledTextField(
                title = stringResource(R.string.client_name),
                initialValue = reservation.clientName,
                onValueChanged = { newText ->
                    action(
                        ReservationUiAction.UpdateCustomerName(
                            newText
                        )
                    )
                },
                isOptional = false,
                keyboardType = KeyboardType.Text
            )
            TitledDropDownTextField(
                title = "CustomerCountry",
                selectedValue = reservation.clientCountry,
                onValueChanged = { index ->
                    action(
                        ReservationUiAction.UpdateCustomerCountry(
                            country = countries[index]
                        )
                    )
                },
                isOptional = false,
                options = countries,
            )
            TitledTextField(
                title = "Travel No",
                initialValue = reservation.flightNumber,
                onValueChanged = { newText ->
                    action(
                        ReservationUiAction.UpdateTravelNumber(
                            newText
                        )
                    )
                },
                isOptional = true,
                keyboardType = KeyboardType.Text,
            )

            TitledDropDownTextField(
                title = "Tourist Company",
                selectedValue = reservation.tourismCompany,
                onValueChanged = { index ->
                    action(
                        ReservationUiAction.UpdateSelectedTourismCompany(
                            company = tourismCompanies[index]
                        )
                    )
                },
                isOptional = true,
                options = tourismCompanies.map { it.name },
            )
            TitledDropDownTextField(
                title = "Tourism Employee",
                selectedValue = reservation.tourismEmployee,
                onValueChanged = { index ->
                    action(
                        ReservationUiAction.UpdateTourismEmployee(
                            employee = employees[index]
                        )
                    )
                },
                isOptional = true,
                options = employees.map { it.name },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.rides),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { isAddMovementSheetVisible = true },
                    enabled = reservation.clientPhone.isNotBlank() &&
                            reservation.tourismCompany.isNotBlank() &&
                            reservation.tourismEmployee.isNotBlank(),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Ride"
                    )
                }
            }
            rides.forEach {ride->
                RideItem(ride = ride)
            }

            Spacer(modifier = Modifier.height(16.dp))
            if (rides.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp),
                        onClick = { action(ReservationUiAction.AddReservation) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.save_reservation))
                    }
                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp),
                        shape = RoundedCornerShape(12.dp),
                        onClick = { action(ReservationUiAction.Cancel) }
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            }

        }
        if (isAddMovementSheetVisible) {
            AddRideBottomSheet(
                addMovementSheetState,
                errorMessage,
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


@Preview
@Composable
fun NewClientDataEntryScreenPreview() {
    ReservationManagerTheme {
        CreateReservationScreenContent()
    }
}