package com.zaed.reservationmanager.ui.reservation.create.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.ui.components.TitledDropDownTextField
import com.zaed.reservationmanager.ui.components.TitledTextField
import com.zaed.reservationmanager.ui.components.TitledTextField2
import com.zaed.reservationmanager.ui.home.component.DetailRow
import com.zaed.reservationmanager.ui.reservation.create.ReservationError

@Composable
fun CustomerInfoSection(
    modifier: Modifier = Modifier,
    customer: Customer = Customer(),
    isNewCustomer: Boolean? = null,
    onSearchCustomer: () -> Unit = {},
    onUpdateName: (String) -> Unit = {},
    onUpdateCountry: (String) -> Unit = {},
    countries: List<String> = emptyList(),
    onUpdatePhoneNumber: (String) -> Unit = {},
    onUpdateEmail: (String) -> Unit = {},
    error: ReservationError = ReservationError.NONE,
    onUpdateNationality: (String) -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TitledTextField2(
            title = stringResource(R.string.client_phone),
            value = customer.phoneNumber,
            onValueChanged = { phoneNumber ->
                onUpdatePhoneNumber(phoneNumber)
            },
            isOptional = false,
            isError = (error == ReservationError.CUSTOMER_PHONE_IS_REQUIRED || error == ReservationError.CUSTOMER_PHONE_IS_INVALID),
            errorMessageRes = error.messageRes,
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Search,
            keyboardActions = KeyboardActions(
                onSearch = {
                    onSearchCustomer()
                }
            ),
        )
        AnimatedVisibility(isNewCustomer != null) {
            when {
                isNewCustomer == true -> {
                    TitledTextField2(
                        title = stringResource(R.string.client_name),
                        value = customer.name,
                        onValueChanged = { newText ->
                            onUpdateName(newText)
                        },
                        isOptional = false,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                        isError = (error == ReservationError.CUSTOMER_NAME_IS_REQUIRED),
                        errorMessageRes = error.messageRes,
                    )
                    TitledTextField(
                        title = stringResource(R.string.nationality),
                        initialValue = customer.nationality,
                        onValueChanged = { nationality ->
                            onUpdateNationality(nationality)
                        },
                        isOptional = true,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                    )
                    TitledDropDownTextField(
                        title = stringResource(R.string.residence_country),
                        selectedValue = customer.residenceCountry,
                        onValueChanged = { index ->
                            onUpdateCountry(countries[index])
                        },
                        isOptional = false,
                        options = countries,
                        isError = (error == ReservationError.CUSTOMER_COUNTRY_IS_REQUIRED),
                        errorMessageRes = error.messageRes,
                    )
                    TitledDropDownTextField(
                        title = stringResource(R.string.email),
                        selectedValue = customer.email,
                        onValueChanged = { index ->
                            onUpdateEmail(countries[index])
                        },
                        isOptional = true,
                        options = countries,
                        isError = (error == ReservationError.EMAIL_IS_INVALID),
                        errorMessageRes = error.messageRes,
                    )

                }

                else -> {
                    DetailRow(
                        label = stringResource(R.string.client_name),
                        value = customer.name,
                    )
                    DetailRow(
                        label = stringResource(R.string.nationality),
                        value = customer.nationality
                    )
                    DetailRow(
                        label = stringResource(R.string.residence_country),
                        value = customer.residenceCountry
                    )
                    DetailRow(
                        label = stringResource(R.string.email),
                        value = customer.email,
                        isDividerVisible = false
                    )
                }
            }
        }
    }
}