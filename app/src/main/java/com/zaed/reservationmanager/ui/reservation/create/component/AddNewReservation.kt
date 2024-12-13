package com.zaed.reservationmanager.ui.reservation.create.component

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.ui.components.TitledDropDownTextField
import com.zaed.reservationmanager.ui.components.TitledTextField
import com.zaed.reservationmanager.ui.reservation.create.ReservationError
import com.zaed.reservationmanager.ui.reservation.create.ReservationUiAction

@Composable
fun AddNewReservation(
    initialReservation: Reservation,
    reservation: Reservation,
    action: (ReservationUiAction) -> Unit,
    errorMessage: ReservationError,
    countries: List<String>,
    tourismCompanies: List<Company>,
    employees: List<Employee>
) {
    TitledTextField(
        title = stringResource(R.string.client_phone),
        initialValue = initialReservation.clientPhone,
        onValueChanged = { newText ->
            action(
                ReservationUiAction.UpdateCustomerNumber(
                    newText
                )
            )
        },
        isOptional = false,
        isError = (errorMessage == ReservationError.CUSTOMER_PHONE_IS_REQUIRED||errorMessage == ReservationError.CUSTOMER_PHONE_IS_INVALID),
        errorMessageRes = errorMessage.messageRes,
        keyboardType = KeyboardType.Phone,
        imeAction = ImeAction.Search,
        keyboardActions = KeyboardActions(
            onSearch = {
                action(ReservationUiAction.SearchClient)
            }
        ),
    )
    TitledTextField(
        title = stringResource(R.string.client_name),
        initialValue = initialReservation.clientName,
        onValueChanged = { newText ->
            action(
                ReservationUiAction.UpdateCustomerName(
                    newText
                )
            )
        },
        isOptional = false,
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Next,
        isError = (errorMessage == ReservationError.CUSTOMER_NAME_IS_REQUIRED),
        errorMessageRes = errorMessage.messageRes,
    )
    TitledDropDownTextField(
        title = stringResource(R.string.customer_country),
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
        isError = (errorMessage == ReservationError.CUSTOMER_COUNTRY_IS_REQUIRED),
        errorMessageRes = errorMessage.messageRes,
    )
    TitledTextField(
        title = stringResource(R.string.travel_no),
        initialValue = initialReservation.flightNumber,
        onValueChanged = { newText ->
            action(
                ReservationUiAction.UpdateTravelNumber(
                    newText
                )
            )
        },
        isOptional = true,
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Next,
    )

    TitledDropDownTextField(
        title = stringResource(R.string.tourist_company),
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
        isError = (errorMessage == ReservationError.TOURISM_COMPANY_IS_REQUIRED),
        errorMessageRes = errorMessage.messageRes,
    )
    TitledDropDownTextField(
        title = stringResource(R.string.tourism_employee),
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
        isError = (errorMessage == ReservationError.EMPLOYEE_IS_REQUIRED),
        errorMessageRes = errorMessage.messageRes,
    )
}