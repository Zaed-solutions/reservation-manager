package com.zaed.reservationmanager.ui.home.component

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.ui.components.TitledDropDownTextField
import com.zaed.reservationmanager.ui.components.TitledTextField2
import com.zaed.reservationmanager.ui.reservation.create.ReservationError
import com.zaed.reservationmanager.ui.reservation.create.component.DatePickerFieldToModal
import com.zaed.reservationmanager.ui.reservation.create.component.TimePickerFieldToModal
import com.zaed.reservationmanager.ui.util.InputValidator

@Composable
fun AddReservationBottomSheetContent(
    modifier: Modifier = Modifier,
    tourismCompanies: List<Company> = emptyList(),
    employees: List<Employee> = emptyList(),
    onFetchEmployees: (String) -> Unit = {},
    initialReservation: Reservation = Reservation(),
    types: List<String> = emptyList(),
    cars: List<String> = emptyList(),
    travelCompanies: List<Company> = emptyList(),
    onFetchDrivers: (String) -> Unit = {},
    drivers: List<Employee> = emptyList(),
    onSaveReservation: (Reservation) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    var reservation by remember {
        mutableStateOf(initialReservation)
    }
    var reservationError by remember {
        mutableStateOf(ReservationError.NONE)
    }
    val peopleCounter by remember {
        mutableStateOf((1..50).toList())
    }
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        DatePickerFieldToModal(
            initialValue = initialReservation.date,
            errorMessage = reservationError,
            onDateSelected = { newDate ->
                reservation = reservation.copy(
                    date = newDate ?: 0L
                )
            }
        )
        TimePickerFieldToModal(
            initialValue = initialReservation.time,
            errorMessage = reservationError,
            onTimeSelected = { data ->
                reservation = reservation.copy(
                    time = data ?: 0L,
                )
            }
        )
        TitledTextField2(
            title = stringResource(R.string.flight_number),
            value = reservation.flightNumber,
            onValueChanged = { newText ->
                reservation = reservation.copy(
                    flightNumber = newText
                )
            },
            isOptional = true,
            isError = false,
            errorMessageRes = reservationError.messageRes,
            keyboardType = KeyboardType.Text
        )
        TitledDropDownTextField(
            title = stringResource(R.string.tourism_company),
            selectedValue = reservation.tourismCompany,
            onValueChanged = { index ->
                if (index == -1) {
                    reservation = reservation.copy(
                        tourismCompany = "",
                        tourismCompanyPhone = "",
                        tourismCompanyId = "",
                        tourismEmployeePhone = "",
                        tourismEmployeeId = "",
                        tourismEmployee = ""
                    )
                } else {
                    val company = tourismCompanies[index]
                    reservation = reservation.copy(
                        tourismCompany = company.name,
                        tourismCompanyPhone = company.phoneNumber,
                        tourismCompanyId = company.id,
                        tourismEmployeePhone = "",
                        tourismEmployeeId = "",
                        tourismEmployee = ""
                    )
                    onFetchEmployees(company.id)
                }
            },
            isOptional = true,
            options = tourismCompanies.map { it.name },
        )
        TitledDropDownTextField(
            title = stringResource(R.string.employee_name),
            selectedValue = reservation.tourismEmployee,
            onValueChanged = { index ->
                if (index == -1) {
                    reservation = reservation.copy(
                        tourismEmployeeId = "",
                        tourismEmployee = "",
                        tourismEmployeePhone = ""
                    )
                } else {
                    val employee = employees[index]
                    reservation = reservation.copy(
                        tourismEmployeeId = employee.id,
                        tourismEmployee = employee.name,
                        tourismEmployeePhone = employee.phoneNumber1
                    )
                }
            },
            isOptional = true,
            options = employees.map { it.name },
        )
        TitledDropDownTextField(
            title = stringResource(R.string.reservation_type),
            selectedValue = reservation.type,
            onValueChanged = { index ->
                if (index != -1) {
                    reservation = reservation.copy(
                        type = types[index]
                    )
                }
            },
            isOptional = false,
            isError = reservationError == ReservationError.TYPE_IS_REQUIRED,
            errorMessageRes = reservationError.messageRes,
            options = types,
        )
        TitledDropDownTextField(
            title = stringResource(R.string.travel_company),
            selectedValue = reservation.travelCompany,
            onValueChanged = { index ->
                if (index == -1) {
                    reservation = reservation.copy(
                        travelCompany = "",
                        travelCompanyPhone = "",
                        travelCompanyId = "",
                        driverId = "",
                        driver = "",
                        driverPhoneNumber = ""
                    )
                } else {
                    val company = travelCompanies[index]
                    reservation = reservation.copy(
                        travelCompany = company.name,
                        travelCompanyPhone = company.phoneNumber,
                        travelCompanyId = company.id,
                        driverId = "",
                        driver = "",
                        driverPhoneNumber = ""
                    )
                    onFetchDrivers(company.id)
                }
            },
            isOptional = true,
            options = travelCompanies.map { it.name },
        )
        TitledDropDownTextField(
            title = stringResource(R.string.driver_name),
            selectedValue = reservation.driver,
            onValueChanged = { index ->
                if (index == -1) {
                    reservation = reservation.copy(
                        driverId = "",
                        driver = "",
                        driverPhoneNumber = ""
                    )
                } else {
                    val driver = drivers[index]
                    reservation = reservation.copy(
                        driverId = driver.id,
                        driver = driver.name,
                        driverPhoneNumber = driver.phoneNumber1
                    )
                }
            },
            isOptional = true,
            options = drivers.map { it.name },
        )
        TitledDropDownTextField(
            title = stringResource(R.string.car),
            selectedValue = reservation.car,
            onValueChanged = { index ->
                if (index == -1) {
                    reservation = reservation.copy(
                        car = ""
                    )
                } else {
                    reservation = reservation.copy(
                        car = cars[index]
                    )
                }
            },
            isOptional = true,
            isError = reservationError == ReservationError.CAR_IS_REQUIRED,
            errorMessageRes = reservationError.messageRes,
            options = cars,
        )
        TitledDropDownTextField(
            title = stringResource(R.string.people_count),
            selectedValue = reservation.peopleCount.toString(),
            onValueChanged = { index ->
                if (index == -1) {
                    reservation = reservation.copy(
                        peopleCount = 1
                    )
                } else {
                    reservation = reservation.copy(
                        peopleCount = peopleCounter[index]
                    )
                }
            },
            isOptional = true,
            options = peopleCounter.map { it.toString() },
        )
        AnimatedVisibility(visible = reservation.car.isNotBlank()) {
            TitledDropDownTextField(
                title = stringResource(R.string.car_count),
                selectedValue = reservation.carCount.toString(),
                onValueChanged = { index ->
                    if (index == -1) {
                        reservation = reservation.copy(
                            carCount = 1
                        )
                    } else {
                        reservation = reservation.copy(
                            carCount = index + 1
                        )
                    }
                },
                options = (1..10).map { it.toString() },
                isOptional = true,
                isError = false,
                errorMessageRes = reservationError.messageRes,
            )
        }
        TitledTextField2(
            title = stringResource(R.string.start_location),
            value = reservation.startLocation,
            onValueChanged = { newText ->
                reservation = reservation.copy(
                    startLocation = newText
                )
                Log.d("AddReservationBottomSheet", "AddReservationBottomSheetContent: $newText")
            },
            isOptional = false,
            isError = reservationError == ReservationError.START_LOCATION_IS_REQUIRED,
            errorMessageRes = reservationError.messageRes,
            keyboardType = KeyboardType.Text
        )
        TitledTextField2(
            title = stringResource(R.string.end_location),
            value = reservation.endLocation,
            onValueChanged = { newText ->
                reservation = reservation.copy(
                    endLocation = newText
                )
            },
            isOptional = false,
            isError = reservationError == ReservationError.END_LOCATION_IS_REQUIRED,
            errorMessageRes = reservationError.messageRes,
            keyboardType = KeyboardType.Text
        )
        AnimatedVisibility(visible = reservation.tourismCompany.isNotBlank()) {
            Column {
                TitledTextField2(
                    title = "Tourism Ride Price",
                    value = reservation.tourismRidePrice.toString(),
                    onValueChanged = { newText ->
                        if (newText.isNotBlank() && newText.matches(Regex("^\\d+\\.?\\d*\$"))) {
                            reservation = reservation.copy(
                                tourismRidePrice = newText.toInt()
                            )
                        }
                    },
                    isOptional = false,
                    isError = reservationError == ReservationError.RIDE_PRICE_IS_REQUIRED,
                    errorMessageRes = reservationError.messageRes,
                    keyboardType = KeyboardType.Number
                )
                TitledTextField2(
                    title = "Tourism Collected Amount",
                    value = reservation.tourismCollectedAmount.toString(),
                    onValueChanged = { newText ->
                        if (newText.isNotBlank() && newText.matches(Regex("^\\d+\\.?\\d*\$"))) { // Accepts digits and an optional decimal point
                            reservation = reservation.copy(
                                tourismCollectedAmount = newText.toInt()
                            )
                        }
                    },
                    isOptional = true,
                    isError = reservationError == ReservationError.COLLECTING_PRICE_IS_REQUIRED,
                    errorMessageRes = reservationError.messageRes,
                    keyboardType = KeyboardType.Number
                )
                TitledTextField2(
                    title = "Tourism Company Quota",
                    value = (reservation.tourismRidePrice - reservation.tourismCollectedAmount).toString(),
                    isOptional = true,
                    isError = reservationError == ReservationError.COLLECTING_PRICE_IS_REQUIRED,
                    errorMessageRes = reservationError.messageRes,
                    keyboardType = KeyboardType.Number
                )
            }
        }
        AnimatedVisibility(visible = reservation.travelCompany.isNotBlank()) {
            Column {
                TitledTextField2(
                    title = "Travel Ride Price",
                    value = reservation.travelRidePrice.toString(),
                    onValueChanged = { newText ->
                        if (newText.matches(Regex("^\\d+\\.?\\d*\$"))) { // Accepts digits and an optional decimal point
                            reservation = reservation.copy(
                                travelRidePrice = newText.toInt()
                            )
                        } else {
                            reservation = reservation.copy(
                                travelRidePrice = 0
                            )
                        }
                    },
                    isOptional = false,
                    isError = reservationError == ReservationError.RIDE_PRICE_IS_REQUIRED,
                    errorMessageRes = reservationError.messageRes,
                    keyboardType = KeyboardType.Number
                )
                TitledTextField2(
                    title = "Travel Collected Amount",
                    value = reservation.travelCollectedAmount.toString(),
                    onValueChanged = { newText ->
                        if (newText.matches(Regex("^\\d+\\.?\\d*\$"))) { // Accepts digits and an optional decimal point
                            reservation = reservation.copy(
                                travelCollectedAmount = newText.toInt()
                            )
                        } else {
                            reservation = reservation.copy(
                                travelCollectedAmount = 0
                            )
                        }
                    },
                    isOptional = true,
                    isError = reservationError == ReservationError.COLLECTING_PRICE_IS_REQUIRED,
                    errorMessageRes = reservationError.messageRes,
                    keyboardType = KeyboardType.Number
                )
                TitledTextField2(
                    title = "Travel Company Quota",
                    value = (reservation.travelRidePrice - reservation.travelCollectedAmount).toString(),
                    isOptional = true,
                    isError = reservationError == ReservationError.COLLECTING_PRICE_IS_REQUIRED,
                    errorMessageRes = reservationError.messageRes,
                    keyboardType = KeyboardType.Number,
                    isEnabled = false,
                    isReadOnly = true
                )
            }
        }

        TitledTextField2(
            title = stringResource(R.string.notes),
            value = reservation.note,
            onValueChanged = { newText ->
                reservation = reservation.copy(
                    note = newText
                )
            },
            maxLines = 10,
            imeAction = ImeAction.Default,
            isOptional = true,
            keyboardType = KeyboardType.Text
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(
                modifier = Modifier.weight(1f),
                onClick = { onDismiss() },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.cancel)
                )
            }
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    val error = InputValidator.validateRide(reservation)
                    if (error != null) {
                        reservationError = error
                    } else {
                        onSaveReservation(reservation)
                        reservation = Reservation()
                        reservationError = ReservationError.NONE
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.save_ride))
            }
        }
    }
}