package com.zaed.reservationmanager.ui.reservation.create

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
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
import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.data.model.Ride
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.ui.components.TitledDropDownTextField
import com.zaed.reservationmanager.ui.components.TitledTextField
import com.zaed.reservationmanager.ui.reservation.create.component.CenterAlignedTopBar
import com.zaed.reservationmanager.ui.reservation.create.component.DatePickerFieldToModal
import com.zaed.reservationmanager.ui.reservation.create.component.TimePickerFieldToModal
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
        customer = state.customer,
        types = state.transactionTypes,
        cars = state.carTypes,
        drivers = state.drivers,
        selectedTravelCompany = state.selectedTravelCompany,
        selectedTourismCompany = state.selectedTourismCompany,
        action = viewModel::handleAction,
        isFieldsEnabled = state.isFieldsEnabled,
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
    travelCompanies: List<String> = emptyList(),
    tourismCompanies: List<String> = emptyList(),
    types: List<String> = emptyList(),
    cars: List<String> = emptyList(),
    drivers: List<String> = emptyList(),
    rides: List<Ride> = emptyList(),
    newRide: Ride = Ride(),
    employees: List<String> = emptyList(),
    isFieldsEnabled: Boolean = false,
    countries: List<String> = emptyList(),
    customer: Customer = Customer(),
    selectedTravelCompany: String = "",
    selectedTourismCompany: String = "",
    action: (ReservationUiAction) -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: ReservationError = ReservationError.NONE,
    userMessage: String = "",
    onBackClicked: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var isMovementListSheetVisible by remember { mutableStateOf(false) }
    var movementListSheetState = rememberModalBottomSheetState()
    var isAddMovementSheetVisible by remember { mutableStateOf(false) }
    val addMovementSheetState = rememberModalBottomSheetState(true)
    if (userMessage.isNotBlank()) {
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

            Log.d("TAGO", "CreateReservationScreenContent: $customer")

            TitledTextField(
                title = stringResource(R.string.client_phone),
                initialValue = customer.phoneNumber,
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
                initialValue = customer.name,
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
                selectedValue = customer.residenceCountry,
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
                isOptional = false,
                keyboardType = KeyboardType.Text,
            )

            TitledDropDownTextField(
                title = "Tourist Company",
                selectedValue = selectedTourismCompany,
                onValueChanged = { index ->
                    action(
                        ReservationUiAction.UpdateSelectedTourismCompany(
                            company = tourismCompanies[index]
                        )
                    )
                },
                isOptional = true,
                options = tourismCompanies,
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
                options = employees,
            )



            Spacer(modifier = Modifier.height(16.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    isMovementListSheetVisible = true
                    isAddMovementSheetVisible = false
                    action(ReservationUiAction.GetMovementsForUser)
                },
                enabled = if(
                    customer.phoneNumber.isNotBlank() &&
                    selectedTourismCompany.isNotBlank()&&
                    reservation.tourismEmployee.isNotBlank()
                    )true else false,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add New Movement")
            }
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
                        Text(stringResource(R.string.add))
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
        if (isMovementListSheetVisible) {
            ModalBottomSheet(
                onDismissRequest = { isMovementListSheetVisible = false }
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    if (rides.isEmpty()) {
                        Text(text = "No movements yet", modifier = Modifier.padding(32.dp))
                    } else {
                        LazyColumn {
                            items(rides) { movement ->
                                ListItem(
                                    headlineContent = { Text(movement.reservationId) },
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            isMovementListSheetVisible = false
                            isAddMovementSheetVisible = true
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add New Movement")
                    }
                }
            }
        }
        if (isAddMovementSheetVisible) {
            ModalBottomSheet(
                onDismissRequest = { isAddMovementSheetVisible = false },
                sheetState =  addMovementSheetState
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    DatePickerFieldToModal(
                        onDateSelected = { newDate ->
                            action(
                                ReservationUiAction.UpdateReservationDate(
                                    newDate
                                )
                            )
                        }
                    )
                    TimePickerFieldToModal(
                        onTimeSelected = { data ->
                            action(
                                ReservationUiAction.UpdateReservationTime(
                                    time = data
                                )
                            )
                        }
                    )
                    TitledDropDownTextField(
                        title = "Type",
                        selectedValue = newRide.type,
                        onValueChanged = { index ->
                            action(
                                ReservationUiAction.UpdateReservationType(
                                    type = types[index]
                                )
                            )
                        },
                        isOptional = false,
                        isError = errorMessage == ReservationError.TYPE_IS_REQUIRED,
                        errorMessageRes = errorMessage.messageRes,
                        options = types,
                    )

                    TitledDropDownTextField(
                        title = "Car",
                        selectedValue = newRide.car,
                        onValueChanged = { index ->
                            action(
                                ReservationUiAction.UpdateReservationCar(
                                    car = cars[index]
                                )
                            )
                        },
                        isOptional = false,
                        isError = errorMessage == ReservationError.CAR_IS_REQUIRED,
                        errorMessageRes = errorMessage.messageRes,
                        options = cars,
                    )

                    TitledDropDownTextField(
                        title = "Drivers",
                        selectedValue = newRide.driver,
                        onValueChanged = { index ->
                            action(
                                ReservationUiAction.UpdateDriver(
                                    driver = drivers[index]
                                )
                            )
                        },
                        isOptional = true,
                        options = drivers,
                    )
                    TitledTextField(
                        title = stringResource(R.string.start_location),
                        initialValue = newRide.startLocation,
                        onValueChanged = { newText ->
                            action(
                                ReservationUiAction.UpdateStartLocation(
                                    location = newText
                                )
                            )
                        },
                        isOptional = false,
                        isError = errorMessage == ReservationError.CUSTOMER_PHONE_IS_REQUIRED,
                        errorMessageRes = errorMessage.messageRes,
                        keyboardType = KeyboardType.Phone
                    )
                    TitledTextField(
                        title = stringResource(R.string.end_location),
                        initialValue = newRide.endLocation,
                        onValueChanged = { newText ->
                            action(
                                ReservationUiAction.UpdateEndLocation(
                                    location = newText
                                )
                            )
                        },
                        isOptional = false,
                        isError = errorMessage == ReservationError.CUSTOMER_PHONE_IS_REQUIRED,
                        errorMessageRes = errorMessage.messageRes,
                        keyboardType = KeyboardType.Phone
                    )
                    TitledTextField(
                        title = "Buying Price",
                        initialValue = newRide.buyingPrice.toString(),
                        onValueChanged = { newText ->
                            action(
                                ReservationUiAction.UpdateMovementPrice(
                                    price = newText
                                )
                            )
                        },
                        isOptional = false,
                        isError = errorMessage == ReservationError.MOVEMENT_PRICE_IS_REQUIRED,
                        errorMessageRes = errorMessage.messageRes,
                        keyboardType = KeyboardType.Decimal
                    )
                    TitledTextField(
                        title = "Collection Price",
                        initialValue = newRide.collectedPrice.toString(),
                        onValueChanged = { newText ->
                            action(
                                ReservationUiAction.UpdateCollectionPrice(
                                    price = newText
                                )
                            )
                        },
                        isOptional = false,
                        isError = errorMessage == ReservationError.COLLECTION_PRICE_IS_REQUIRED,
                        errorMessageRes = errorMessage.messageRes,
                        keyboardType = KeyboardType.Decimal
                    )
                    TitledTextField(
                        title = "Note",
                        initialValue = newRide.note,
                        onValueChanged = { newText ->
                            action(
                                ReservationUiAction.UpdateNote(
                                    note = newText
                                )
                            )
                        },
                        isOptional = true,
                        keyboardType = KeyboardType.Decimal
                    )
                    TitledDropDownTextField(
                        title = "Travel Company",
                        selectedValue = selectedTravelCompany,
                        onValueChanged = { index ->
                            action(
                                ReservationUiAction.UpdateSelectedTravelCompany(
                                    company = travelCompanies[index]
                                )
                            )
                        },
                        isOptional = true,
                        options = travelCompanies,
                    )
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            action(ReservationUiAction.AddMovement)
                            isAddMovementSheetVisible = false
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Movement")
                    }
                }
            }

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