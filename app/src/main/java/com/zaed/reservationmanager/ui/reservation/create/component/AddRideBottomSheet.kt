package com.zaed.reservationmanager.ui.reservation.create.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.data.model.ReservationModel
import com.zaed.reservationmanager.ui.components.TitledDropDownTextField
import com.zaed.reservationmanager.ui.components.TitledTextField
import com.zaed.reservationmanager.ui.reservation.create.ReservationError
import com.zaed.reservationmanager.ui.reservation.create.ReservationUiAction

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AddRideBottomSheet(
    addMovementSheetState: SheetState,
    errorMessage: ReservationError,
    action: (ReservationUiAction) -> Unit,
    newReservationModel: ReservationModel,
    types: List<String>,
    cars: List<String>,
    travelCompanies: List<Company>,
    drivers: List<Employee>,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = addMovementSheetState
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            DatePickerFieldToModal(
                errorMessage = errorMessage,
                onDateSelected = { newDate ->
                    action(
                        ReservationUiAction.UpdateReservationDate(
                            newDate
                        )
                    )
                }
            )
            TimePickerFieldToModal(
                errorMessage = errorMessage,
                onTimeSelected = { data ->
                    action(
                        ReservationUiAction.UpdateReservationTime(
                            time = data
                        )
                    )
                }
            )
            TitledDropDownTextField(
                title = stringResource(R.string.type),
                selectedValue = newReservationModel.type,
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
                title = stringResource(R.string.car),
                selectedValue = newReservationModel.car,
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
                title = stringResource(R.string.travel_company),
                selectedValue = newReservationModel.travelCompany,
                onValueChanged = { index ->
                    action(
                        ReservationUiAction.UpdateSelectedTravelCompany(
                            company = travelCompanies[index]
                        )
                    )
                },
                isOptional = true,
                options = travelCompanies.map { it.name },
            )
            TitledDropDownTextField(
                title = stringResource(R.string.drivers),
                selectedValue = newReservationModel.driver,
                onValueChanged = { index ->
                    action(
                        ReservationUiAction.UpdateDriver(
                            driver = drivers[index]
                        )
                    )
                },
                isOptional = true,
                options = drivers.map { it.name },
            )
            TitledTextField(
                title = stringResource(R.string.start_location),
                initialValue = newReservationModel.startLocation,
                onValueChanged = { newText ->
                    action(
                        ReservationUiAction.UpdateStartLocation(
                            location = newText
                        )
                    )
                },
                isOptional = false,
                isError = errorMessage == ReservationError.START_LOCATION_IS_REQUIRED,
                errorMessageRes = errorMessage.messageRes,
                keyboardType = KeyboardType.Text
            )
            TitledTextField(
                title = stringResource(R.string.end_location),
                initialValue = newReservationModel.endLocation,
                onValueChanged = { newText ->
                    action(
                        ReservationUiAction.UpdateEndLocation(
                            location = newText
                        )
                    )
                },
                isOptional = false,
                isError = errorMessage == ReservationError.END_LOCATION_IS_REQUIRED,
                errorMessageRes = errorMessage.messageRes,
                keyboardType = KeyboardType.Text
            )
            TitledTextField(
                title = stringResource(R.string.selling_price),
                initialValue = if (newReservationModel.sellingPrice == 0.0) "" else newReservationModel.sellingPrice.toInt().toString(),
                onValueChanged = { newText ->
                    if (newText.isNotBlank() && newText.matches(Regex("^\\d+\\.?\\d*\$"))) { // Accepts digits and an optional decimal point
                        action(
                            ReservationUiAction.UpdateSellingPrice(
                                price = newText
                            )
                        )

                    }else if(newText.isBlank()){
                        action(
                            ReservationUiAction.UpdateSellingPrice(
                                price = "0"
                            )
                        )
                    }
                },
                isOptional = false,
                isError = errorMessage == ReservationError.SELLING_PRICE_IS_REQUIRED,
                errorMessageRes = errorMessage.messageRes,
                keyboardType = KeyboardType.Decimal
            )
            TitledTextField(
                title = stringResource(R.string.buying_price),
                initialValue = if (newReservationModel.buyingPrice == 0.0) "" else newReservationModel.buyingPrice.toInt().toString(),
                onValueChanged = { newText ->
                    if (newText.isNotBlank() && newText.matches(Regex("^\\d+\\.?\\d*\$"))) { // Accepts digits and an optional decimal point
                        action(
                            ReservationUiAction.UpdateBuyingPrice(
                                price = newText
                            )
                        )

                    }else if(newText.isBlank()){
                        action(
                            ReservationUiAction.UpdateBuyingPrice(
                                price = "0"
                            )
                        )
                    }
                },
                isOptional = true,
                keyboardType = KeyboardType.Decimal
            )
            TitledTextField(
                title = stringResource(R.string.collection_price),
                initialValue = if (newReservationModel.collectedAmount == 0.0) "" else newReservationModel.collectedAmount.toInt().toString(),
                onValueChanged = { newText ->
                    if (newText.isNotBlank() && newText.matches(Regex("^\\d+\\.?\\d*\$"))) { // Accepts digits and an optional decimal point
                        action(
                            ReservationUiAction.UpdateCollectionPrice(
                                price = newText
                            )
                        )
                    }else if(newText.isBlank()){
                        action(
                            ReservationUiAction.UpdateCollectionPrice(
                                price = "0"
                            )
                        )
                    }
                },
                isOptional = true,
                isError = false,
                errorMessageRes = errorMessage.messageRes,
                keyboardType = KeyboardType.Decimal
            )
            TitledTextField(
                title = stringResource(R.string.note),
                initialValue = newReservationModel.note,
                onValueChanged = { newText ->

                    action(
                        ReservationUiAction.UpdateNote(
                            note = newText
                        )
                    )
                },
                isOptional = true,
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    action(ReservationUiAction.AddMovement)
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.save_ride))
            }
        }
    }
}