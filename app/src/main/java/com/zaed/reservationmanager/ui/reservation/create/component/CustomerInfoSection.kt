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
import com.zaed.reservationmanager.ui.client.create.CreateCustomerUiAction
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
    onUpdateCity: (String) -> Unit = {},
    countries: List<String> = emptyList(),
    onUpdatePhoneNumber: (String) -> Unit = {},
    onUpdatePhoneNumber2: (String) -> Unit = {},
    onUpdateEmail: (String) -> Unit = {},
    onUpdateJob: (String) -> Unit,
    error: ReservationError = ReservationError.NONE,
    onUpdateNationality: (String) -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TitledTextField2(
            title = stringResource(R.string.client_phone),
            value = customer.phoneNumber1,
            onValueChanged = { phoneNumber ->
                onUpdatePhoneNumber(phoneNumber)
            },
            isOptional = false,
            isError = error in listOf(
                ReservationError.CUSTOMER_PHONE_IS_REQUIRED,
                ReservationError.CUSTOMER_PHONE_IS_INVALID,
                ReservationError.PHONE_NUMBER_1_IS_IN_USE
            ),
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
                    Column(
                        modifier = modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

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
                        TitledDropDownTextField(
                            title = stringResource(R.string.nationality),
                            selectedValue = customer.nationality,
                            onValueChanged = { index ->
                                onUpdateNationality(countries.getOrNull(index) ?: "")
                            },
                            isOptional = true,
                            options = countries,
                        )
                        TitledDropDownTextField(
                            title = stringResource(R.string.residence_country),
                            selectedValue = customer.residenceCountry,
                            onValueChanged = { index ->
                                onUpdateCountry(countries.getOrNull(index) ?: "")
                            },
                            isOptional = false,
                            options = countries,
                            isError = (error == ReservationError.CUSTOMER_COUNTRY_IS_REQUIRED),
                            errorMessageRes = error.messageRes,
                        )
                        TitledTextField2(
                            title = stringResource(R.string.city),
                            value = customer.city,
                            onValueChanged = { newText ->
                                onUpdateCity(newText)
                            },
                            isOptional = true,
                        )
                        TitledTextField2(
                            title = stringResource(R.string.job),
                            value = customer.job,
                            onValueChanged = { newText ->
                                onUpdateJob(newText)
                            },
                            isOptional = true,
                        )
                        TitledTextField(
                            title = stringResource(R.string.email),
                            initialValue = customer.email,
                            onValueChanged = { email ->
                                onUpdateEmail(email)
                            },
                            isOptional = true,
                            isError = (error == ReservationError.EMAIL_IS_INVALID),
                            errorMessageRes = error.messageRes,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next,
                        )
                        TitledTextField(
                            title = stringResource(R.string.phone_number_2),
                            initialValue = customer.phoneNumber2,
                            onValueChanged = { phoneNumber2 ->
                                onUpdatePhoneNumber2(phoneNumber2)
                            },
                            isOptional = true,
                            isError = error in listOf(ReservationError.PHONE_NUMBER_2_IS_IN_USE,),
                            errorMessageRes = error.messageRes,
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next,
                        )
                    }
                }

                else -> {
                    Column(
                        modifier = modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
                            label = stringResource(R.string.city),
                            value = customer.city,
                        )
                        DetailRow(
                            label = stringResource(R.string.job),
                            value = customer.job,
                        )
                        DetailRow(
                            label = stringResource(R.string.email),
                            value = customer.email,
                        )
                        DetailRow(
                            label = stringResource(R.string.phone_number_2),
                            value = customer.phoneNumber2,
                            isDividerVisible = false
                        )
                    }
                }
            }
        }
    }
}