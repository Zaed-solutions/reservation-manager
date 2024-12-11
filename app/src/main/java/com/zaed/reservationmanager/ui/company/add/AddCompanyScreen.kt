package com.zaed.reservationmanager.ui.company.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.CompanyType
import com.zaed.reservationmanager.ui.components.TitledDropDownTextField
import com.zaed.reservationmanager.ui.components.TitledTextField
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddCompanyScreen(
    modifier: Modifier = Modifier,
    initialCompany: Company = Company(),
    viewModel: AddCompanyViewModel = koinViewModel(),
    onBackPressed: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(true) {
        viewModel.init(initialCompany)
    }
    LaunchedEffect(state.isFinished) {
        if (state.isFinished) {
            scope.launch {
                snackbarHostState.showSnackbar(context.getString(if (state.isNew) R.string.company_added_successfully else R.string.company_updated_successfully))
                onBackPressed()
            }
        }
    }
    AddCompanyScreenContent(
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        onAction = { action ->
            when (action) {
                AddCompanyUiAction.OnBackPressed -> onBackPressed()
                else -> viewModel.handleAction(action)
            }
        },
        error = state.error,
        isNew = state.isNew,
        initialCompany = initialCompany,
        company = state.company
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCompanyScreenContent(
    modifier: Modifier = Modifier,
    initialCompany: Company = Company(),
    company: Company = Company(),
    isNew: Boolean = true,
    error: AddCompanyUiError = AddCompanyUiError.NONE,
    onAction: (AddCompanyUiAction) -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        modifier = modifier.imePadding(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    Text(
                        text = if (isNew) stringResource(R.string.add_company) else stringResource(R.string.update_company),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onAction(AddCompanyUiAction.OnBackPressed) }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = null
                        )
                    }
                },
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                shadowElevation = 4.dp
            ) {
                Button(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    shape = MaterialTheme.shapes.medium,
                    onClick = { onAction(AddCompanyUiAction.OnSaveClicked) },
                ) {
                    Text(text = if (isNew) stringResource(R.string.add) else stringResource(R.string.update))
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            //name
            TitledTextField(
                modifier = Modifier.padding(top = 8.dp),
                title = stringResource(R.string.name),
                initialValue = initialCompany.name,
                onValueChanged = {
                    onAction(AddCompanyUiAction.OnNameChanged(it))
                },
                isOptional = false,
                isError = error in listOf(
                    AddCompanyUiError.NAME_IS_REQUIRED,
                    AddCompanyUiError.NAME_IS_ALREADY_USED
                ),
                errorMessageRes = error.messageRes
            )
            //type
            TitledDropDownTextField(
                title = stringResource(R.string.type),
                selectedValue = stringResource(id = company.type.displayNameRes),
                onValueChanged = { index ->
                    onAction(AddCompanyUiAction.OnTypeChanged(index))
                },
                isOptional = true,
                options = CompanyType.entries.map { stringResource(id = it.displayNameRes) },
            )
            //country
            TitledTextField(
                title = stringResource(R.string.country),
                initialValue = initialCompany.country,
                onValueChanged = { country ->
                    onAction(AddCompanyUiAction.OnCountryChanged(country))
                },
                isOptional = false,
                isError = error == AddCompanyUiError.COUNTRY_IS_REQUIRED,
                errorMessageRes = error.messageRes
            )
            //phone number
            TitledTextField(
                title = stringResource(R.string.phone_number),
                initialValue = initialCompany.phoneNumber,
                onValueChanged = { phoneNumber ->
                    onAction(AddCompanyUiAction.OnPhoneNumberChanged(phoneNumber))
                },
                isOptional = true,
                isError = error == AddCompanyUiError.PHONE_NUMBER_IS_INVALID,
                errorMessageRes = error.messageRes,
                keyboardType = KeyboardType.Phone
            )
            //email
            TitledTextField(
                title = stringResource(R.string.email),
                initialValue = initialCompany.email,
                onValueChanged = { email ->
                    onAction(AddCompanyUiAction.OnEmailChanged(email))
                },
                isOptional = true,
                isError = error == AddCompanyUiError.EMAIL_IS_INVALID,
                errorMessageRes = error.messageRes,
                keyboardType = KeyboardType.Email
            )
            //fax number
            TitledTextField(
                modifier = Modifier.padding(bottom = 8.dp),
                title = stringResource(R.string.fax_number),
                initialValue = initialCompany.faxNumber,
                onValueChanged = { faxNumber ->
                    onAction(AddCompanyUiAction.OnFaxNumberChanged(faxNumber))
                },
                isOptional = true,
                isError = error == AddCompanyUiError.FAX_NUMBER_IS_INVALID,
                errorMessageRes = error.messageRes,
                keyboardType = KeyboardType.Phone
            )
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun AddCompanyScreenContentPreview() {
    ReservationManagerTheme {
        AddCompanyScreenContent()
    }
}