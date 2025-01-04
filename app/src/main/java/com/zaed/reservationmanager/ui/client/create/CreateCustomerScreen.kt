package com.zaed.reservationmanager.ui.client.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.ui.components.TitledDropDownTextField
import com.zaed.reservationmanager.ui.components.TitledTextField2
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme
import com.zaed.reservationmanager.ui.util.showSnackbarWithDuration
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddCustomerScreen(
    viewModel: CreateCustomerViewModel = koinViewModel(),
    initialCustomer: Customer,
    navigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember {
        SnackbarHostState()
    }
    val context = LocalContext.current
    LaunchedEffect(true) {
        viewModel.init(initialCustomer)
    }
    LaunchedEffect(state.successStatus) {
        if (state.successStatus) {
            snackbarHostState.showSnackbarWithDuration(
                message = context.getString(
                    if (state.isNew)
                        R.string.customer_added_successfully
                    else
                        R.string.customer_updated_successfully
                ),
                durationMillis = 1500L,
                scope = scope,
                onFinished = {
                    navigateBack()
                }
            )
        }
    }
    NewClientDataEntryScreenContent(
        customer = state.customer,
        error = state.error,
        action = viewModel::handleAction,
        nationalities = state.nationalities,
        countries = state.countries,
        isLoading = state.loading,
        isNew = state.isNew,
        onBackClicked = navigateBack,
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun NewClientDataEntryScreenContent(
    customer: Customer = Customer(),
    isNew: Boolean = true,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    error: ClientUIError = ClientUIError.NONE,
    action: (CreateCustomerUiAction) -> Unit = {},
    nationalities: List<String> = emptyList(),
    countries: List<String> = emptyList(),
    isLoading: Boolean = false,
    onBackClicked: () -> Unit = {},
) {
    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        topBar = {
            CenterAlignedTopBar(
                onBackClicked = onBackClicked,
                title = if (isNew) stringResource(R.string.create_new_customer) else stringResource(
                    R.string.update_customer
                )
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    modifier = Modifier
                        .weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    onClick = { onBackClicked() }
                ) {
                    Text(stringResource(R.string.cancel))
                }
                Button(
                    modifier = Modifier
                        .weight(1f),
                    onClick = { action(CreateCustomerUiAction.SubmitClient) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isNew) stringResource(R.string.add) else stringResource(R.string.update))
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            TitledTextField2(
                title = stringResource(R.string.client_name),
                value = customer.name,
                onValueChanged = { newText -> action(CreateCustomerUiAction.UpdateName(newText)) },
                isOptional = false,
                isError = error in listOf(
                    ClientUIError.NAME_IS_REQUIRED
                ),
                errorMessageRes = error.messageRes,
                keyboardType = KeyboardType.Text
            )
            TitledDropDownTextField(
                title = stringResource(R.string.nationality),
                selectedValue = customer.nationality,
                onValueChanged = { index ->
                    action(
                        CreateCustomerUiAction.UpdateNationality(
                            nationalities.getOrNull(index) ?: ""
                        )
                    )
                },
                isOptional = true,
                isError = false,
                errorMessageRes = R.string.nationality_is_required,
                options = nationalities
            )
            TitledDropDownTextField(
                title = stringResource(R.string.country_of_residence),
                selectedValue = customer.residenceCountry,
                onValueChanged = { index ->
                    action(
                        CreateCustomerUiAction.UpdateCountry(
                            countries.getOrNull(
                                index
                            ) ?: ""
                        )
                    )
                },
                isOptional = false,
                errorMessageRes = R.string.country_is_required,
                options = countries
            )
            TitledTextField2(
                title = stringResource(R.string.city),
                value = customer.city,
                onValueChanged = { newText ->
                    action(CreateCustomerUiAction.UpdateCity(newText))
                },
                isOptional = true,
            )
            TitledTextField2(
                title = stringResource(R.string.phone_number_1),
                value = customer.phoneNumber1,
                onValueChanged = { newText ->
                    action(CreateCustomerUiAction.UpdateNumber1(newText))
                },
                isOptional = false,
                isError = error in listOf(
                    ClientUIError.PHONE_NUMBER_IS_REQUIRED,
                    ClientUIError.PHONE_NUMBER_1_IS_IN_USE,
                    ClientUIError.PHONE_NUMBER_1_IS_INVALID
                ),
                errorMessageRes = error.messageRes,
                keyboardType = KeyboardType.Phone
            )
            TitledTextField2(
                title = stringResource(R.string.phone_number_2),
                value = customer.phoneNumber2,
                onValueChanged = { newText ->
                    action(CreateCustomerUiAction.UpdateNumber2(newText))
                },
                isError = (error in listOf(
                    ClientUIError.PHONE_NUMBER_2_IS_INVALID,
                    ClientUIError.PHONE_NUMBER_2_IS_IN_USE
                )) && customer.phoneNumber2.isNotBlank(),
                errorMessageRes = error.messageRes,
                isOptional = true,
                keyboardType = KeyboardType.Phone
            )
            TitledTextField2(
                title = stringResource(R.string.email),
                value = customer.email,
                onValueChanged = { newText -> action(CreateCustomerUiAction.UpdateEmail(newText)) },
                isOptional = true,
                isError = error in listOf(ClientUIError.EMAIL_IS_INVALID),
                errorMessageRes = error.messageRes,
                keyboardType = KeyboardType.Email
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CenterAlignedTopBar(
    onBackClicked: () -> Unit = {},
    title: String
) {
    CenterAlignedTopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBackClicked) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }
    )
}


@Preview
@Composable
fun NewClientDataEntryScreenPreview() {
    ReservationManagerTheme {
        NewClientDataEntryScreenContent()
    }
}