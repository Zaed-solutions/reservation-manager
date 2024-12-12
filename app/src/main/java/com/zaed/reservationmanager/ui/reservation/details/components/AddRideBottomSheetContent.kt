package com.zaed.reservationmanager.ui.reservation.details.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.data.model.Ride
import com.zaed.reservationmanager.ui.components.TitledDropDownTextField
import com.zaed.reservationmanager.ui.components.TitledTextField
import com.zaed.reservationmanager.ui.reservation.create.ReservationError
import com.zaed.reservationmanager.ui.reservation.create.component.DatePickerFieldToModal
import com.zaed.reservationmanager.ui.reservation.create.component.TimePickerFieldToModal

@Composable
fun AddRideBottomSheetContent(
    modifier: Modifier = Modifier,
    types: List<String> = emptyList(),
    cars: List<String> = emptyList(),
    travelCompanies: List<Company> = emptyList(),
    drivers: List<Employee> = emptyList(),
    onAddRide: (Ride) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    var newRide = remember {
        Ride()
    }
    var rideError = remember {
        ReservationError.NONE
    }
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        DatePickerFieldToModal(
            errorMessage = rideError,
            onDateSelected = { newDate ->
                newRide = newRide.copy(
                    date = newDate ?: 0L
                )
            }
        )
        TimePickerFieldToModal(
            errorMessage = rideError,
            onTimeSelected = { data ->
                newRide = newRide.copy(
                    date = newRide.date + (data ?: 0L),
                )
            }
        )
        TitledDropDownTextField(
            title = stringResource(R.string.type),
            selectedValue = newRide.type,
            onValueChanged = { index ->
                newRide = newRide.copy(
                    type = types[index]
                )
            },
            isOptional = false,
            isError = rideError == ReservationError.TYPE_IS_REQUIRED,
            errorMessageRes = rideError.messageRes,
            options = types,
        )

        TitledDropDownTextField(
            title = stringResource(R.string.car),
            selectedValue = newRide.car,
            onValueChanged = { index ->
                newRide = newRide.copy(
                    car = cars[index]
                )
            },
            isOptional = false,
            isError = rideError == ReservationError.CAR_IS_REQUIRED,
            errorMessageRes = rideError.messageRes,
            options = cars,
        )
        TitledDropDownTextField(
            title = stringResource(R.string.travel_company),
            selectedValue = newRide.travelCompany,
            onValueChanged = { index ->
                val company = travelCompanies[index]
                newRide = newRide.copy(
                    travelCompany = company.name,
                    travelCompanyPhone = company.phoneNumber,
                    travelCompanyId = company.id
                )
            },
            isOptional = true,
            options = travelCompanies.map { it.name },
        )
        TitledDropDownTextField(
            title = stringResource(R.string.drivers),
            selectedValue = newRide.driver,
            onValueChanged = { index ->
                val driver = drivers[index]
                newRide = newRide.copy(
                    driverId = driver.id,
                    driver = driver.name,
                    driverPhoneNumber = driver.phoneNumber1
                )
            },
            isOptional = true,
            options = drivers.map { it.name },
        )
        TitledTextField(
            title = stringResource(R.string.start_location),
            initialValue = newRide.startLocation,
            onValueChanged = { newText ->
                newRide = newRide.copy(
                    startLocation = newText
                )
            },
            isOptional = false,
            isError = rideError == ReservationError.START_LOCATION_IS_REQUIRED,
            errorMessageRes = rideError.messageRes,
            keyboardType = KeyboardType.Text
        )
        TitledTextField(
            title = stringResource(R.string.end_location),
            initialValue = newRide.endLocation,
            onValueChanged = { newText ->
                newRide = newRide.copy(
                    endLocation = newText
                )
            },
            isOptional = false,
            isError = rideError == ReservationError.END_LOCATION_IS_REQUIRED,
            errorMessageRes = rideError.messageRes,
            keyboardType = KeyboardType.Text
        )
        TitledTextField(
            title = stringResource(R.string.buying_price),
            initialValue = if (newRide.buyingPrice == 0.0) "" else newRide.buyingPrice.toString(),
            onValueChanged = { newText ->
                if (newText.isNotBlank() && newText.matches(Regex("^\\d+\\.?\\d*\$"))) { // Accepts digits and an optional decimal point
                    newRide = newRide.copy(
                        buyingPrice = newText.toDouble()
                    )
                }
            },
            isOptional = false,
            isError = rideError == ReservationError.BUYING_PRICE_IS_REQUIRED,
            errorMessageRes = rideError.messageRes,
            keyboardType = KeyboardType.Decimal
        )
        TitledTextField(
            title = stringResource(R.string.collection_price),
            initialValue = if (newRide.collectedPrice == 0.0) "" else newRide.collectedPrice.toString(),
            onValueChanged = { newText ->
                if (newText.isNotBlank() && newText.matches(Regex("^\\d+\\.?\\d*\$"))) { // Accepts digits and an optional decimal point
                    newRide = newRide.copy(
                        collectedPrice = newText.toDouble()
                    )
                }
            },
            isOptional = false,
            isError = rideError == ReservationError.COLLECTION_PRICE_IS_REQUIRED,
            errorMessageRes = rideError.messageRes,
            keyboardType = KeyboardType.Decimal
        )
        TitledTextField(
            title = stringResource(R.string.note),
            initialValue = newRide.note,
            onValueChanged = { newText ->
                newRide = newRide.copy(
                    note = newText
                )
            },
            isOptional = true,
            keyboardType = KeyboardType.Decimal
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(onClick = { onDismiss() }) {
                Text(
                    text = stringResource(id = R.string.cancel)
                )
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onAddRide(newRide)
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.save_ride))
            }

        }
    }
}