package com.zaed.reservationmanager.ui.client.create

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.ui.client.ClientUIError
import com.zaed.reservationmanager.ui.components.TitledDropDownTextField
import com.zaed.reservationmanager.ui.components.TitledTextField
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun NewClientDataEntryScreen(
    viewModel: CreateCustomerViewModel = koinViewModel(),
    navigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state.successStatus) {
        if (state.successStatus) {
            navigateBack()
        }
    }
    NewClientDataEntryScreenContent(
        clientName = state.clientName,
        clientNameError = state.clientNameError,
        nationality = state.nationality,
        countryOfResidence = state.countryOfResidence,
        mobile = state.mobile,
        mobileError = state.mobileError,
        email = state.email,
        emailError = state.emailError,
        action = viewModel::handleAction,
        nationalities = state.nationalities,
        countries = state.countries,
        isLoading = state.loading,
        errorMessage = state.errorMessage,
        onBackClicked = navigateBack
    )
}

@Composable
fun NewClientDataEntryScreenContent(
    clientName: String = "",
    clientNameError: ClientUIError = ClientUIError.NONE,
    nationality: String = "",
    countryOfResidence: String = "",
    mobile: String = "",
    mobileError: ClientUIError = ClientUIError.NONE,
    email: String = "",
    emailError: ClientUIError = ClientUIError.NONE,
    action: (CreateCustomerUiAction) -> Unit = {},
    nationalities: List<String> = emptyList(),
    countries: List<String> = emptyList(),
    isLoading: Boolean = false,
    errorMessage: ClientUIError = ClientUIError.NONE,
    onBackClicked: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    if (errorMessage != ClientUIError.NONE) {
        val errrorString = stringResource(errorMessage.messageRes)
        LaunchedEffect(true) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = errrorString
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
                title = stringResource(R.string.create_new_customer)
            )
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
                LinearProgressIndicator()
            }
            TitledTextField(
                title = stringResource(R.string.client_name),
                initialValue = clientName,
                onValueChanged = { newText -> action(CreateCustomerUiAction.UpdateName(newText)) },
                isOptional = false,
                isError = clientNameError != ClientUIError.NONE,
                errorMessageRes = clientNameError.messageRes,
                keyboardType = KeyboardType.Text
            )
            TitledDropDownTextField(
                title = stringResource(R.string.nationality),
                selectedValue = nationality,
                onValueChanged = { index ->
                    action(
                        CreateCustomerUiAction.UpdateNationality(
                            nationalities[index]
                        )
                    )
                },
                isOptional = false,
                isError = nationality.isBlank(),
                errorMessageRes = R.string.nationality_is_required,
                options = nationalities
            )
            TitledDropDownTextField(
                title = stringResource(R.string.country_of_residence),
                selectedValue = countryOfResidence,
                onValueChanged = { index -> action(CreateCustomerUiAction.UpdateCountry(countries[index])) },
                isOptional = false,
                errorMessageRes = R.string.country_is_required,
                options = countries
            )
            TitledTextField(
                title = "Mobile",
                initialValue = mobile,
                onValueChanged = { newText -> action(CreateCustomerUiAction.UpdateNumber(newText)) },
                isOptional = false,
                isError = mobileError != ClientUIError.NONE,
                errorMessageRes = mobileError.messageRes,
                keyboardType = KeyboardType.Phone
            )
            TitledTextField(
                title = stringResource(R.string.email),
                initialValue = email,
                onValueChanged = { newText -> action(CreateCustomerUiAction.UpdateEmail(newText)) },
                isOptional = true,
                isError = emailError != ClientUIError.NONE,
                errorMessageRes = emailError.messageRes,
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    onClick = { action(CreateCustomerUiAction.AddClient) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.add))
                }
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    shape = RoundedCornerShape(12.dp),
                    onClick = { action(CreateCustomerUiAction.Cancel) }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
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