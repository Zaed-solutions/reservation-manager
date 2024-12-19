package com.zaed.reservationmanager.ui.employee.add

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.ui.components.TitledDropDownTextField
import com.zaed.reservationmanager.ui.components.TitledTextField
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme
import com.zaed.reservationmanager.ui.util.showSnackbarWithDuration
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddEmployeeScreen(
    modifier: Modifier = Modifier,
    viewModel: AddEmployeeViewModel = koinViewModel(),
    onBackPressed: () -> Unit,
    initialEmployee: Employee = Employee(),
    isDriver: Boolean = false
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(true) {
        viewModel.init(initialEmployee, isDriver)
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    LaunchedEffect(state.isFinished) {
        if (state.isFinished) {
            snackbarHostState.showSnackbarWithDuration(
                message = context.getString(
                    if (state.isNew) {
                        if (isDriver) {
                            R.string.driver_added_successfully
                        } else {
                            R.string.employee_added_successfully
                        }
                    } else {
                        if (isDriver) {
                            R.string.driver_updated_successfully
                        } else {
                            R.string.employee_updated_successfully
                        }
                    }

                ),
                durationMillis = 1500L,
                scope = scope,
                onFinished = {
                    onBackPressed()
                }
            )
        }
    }
    AddEmployeeScreenContent(
        modifier = modifier,
        isDriver = isDriver,
        snackbarHostState = snackbarHostState,
        initialEmployee = initialEmployee,
        currentEmployee = state.employee,
        error = state.error,
        isNew = state.isNew,
        companies = state.companies,
        onAction = { action ->
            when (action) {
                AddEmployeeUiAction.OnBackPressed -> onBackPressed()
                else -> viewModel.handleAction(action)
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEmployeeScreenContent(
    modifier: Modifier = Modifier,
    isDriver: Boolean = false,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    initialEmployee: Employee = Employee(),
    currentEmployee: Employee = Employee(),
    onAction: (AddEmployeeUiAction) -> Unit = {},
    error: AddEmployeeUiError = AddEmployeeUiError.NONE,
    isNew: Boolean = true,
    companies: List<Company> = emptyList()
) {
    Scaffold(
        modifier = modifier.imePadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            val title = if (isDriver && isNew) {
                stringResource(id = R.string.add_new_driver)
            } else if (isDriver) {
                stringResource(id = R.string.update_driver)
            } else if (isNew) {
                stringResource(id = R.string.add_employee)
            } else {
                stringResource(id = R.string.update_employee)
            }
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onAction(AddEmployeeUiAction.OnBackPressed) }
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
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    shape = MaterialTheme.shapes.medium,
                    onClick = { onAction(AddEmployeeUiAction.OnSaveClicked) },
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            //name
            TitledTextField(
                modifier = Modifier.padding(top = 8.dp),
                title = stringResource(R.string.name),
                initialValue = initialEmployee.name,
                onValueChanged = {
                    onAction(AddEmployeeUiAction.OnNameChanged(it))
                },
                isOptional = false,
                isError = error in listOf(
                    AddEmployeeUiError.NAME_IS_REQUIRED,
                    AddEmployeeUiError.NAME_IS_ALREADY_USED
                ),
                errorMessageRes = error.messageRes
            )
            //company
            TitledDropDownTextField(
                title = stringResource(R.string.company),
                selectedValue = currentEmployee.company,
                onValueChanged = { index ->
                    onAction(AddEmployeeUiAction.OnCompanyChanged(companies.getOrElse(index) { Company() }))
                },
                isOptional = false,
                options = companies.map { it.name },
            )
            if (isDriver) {
                // nationality
                TitledTextField(
                    title = stringResource(id = R.string.nationality),
                    initialValue = initialEmployee.nationality,
                    onValueChanged = { nationality ->
                        onAction(AddEmployeeUiAction.OnUpdateNationality(nationality))
                    },
                    isOptional = false,
                    isError = error == AddEmployeeUiError.NATIONALITY_IS_REQUIRED,
                    errorMessageRes = error.messageRes
                )
            } else {
                //position
                TitledTextField(
                    title = stringResource(R.string.position),
                    initialValue = initialEmployee.position,
                    onValueChanged = { position ->
                        onAction(AddEmployeeUiAction.OnPositionChanged(position))
                    },
                    isOptional = true,
                    isError = false,
                    errorMessageRes = error.messageRes
                )
                //email
                TitledTextField(
                    title = stringResource(R.string.email),
                    initialValue = initialEmployee.email,
                    onValueChanged = { email ->
                        onAction(AddEmployeeUiAction.OnEmailChanged(email))
                    },
                    isOptional = true,
                    isError = error == AddEmployeeUiError.EMAIL_IS_INVALID,
                    errorMessageRes = error.messageRes,
                    keyboardType = KeyboardType.Email
                )
            }
            //phone number 1
            TitledTextField(
                title = stringResource(R.string.phone_number_1),
                initialValue = initialEmployee.phoneNumber1,
                onValueChanged = { phoneNumber ->
                    onAction(AddEmployeeUiAction.OnPhoneNumber1Changed(phoneNumber))
                },
                isOptional = false,
                isError = error in listOf(
                    AddEmployeeUiError.PHONE_NUMBER_IS_INVALID,
                    AddEmployeeUiError.PHONE_NUMBER_IS_REQUIRED
                ),
                errorMessageRes = error.messageRes,
                keyboardType = KeyboardType.Phone
            )
            //phone number 2
            TitledTextField(
                title = stringResource(R.string.phone_number_2),
                initialValue = initialEmployee.phoneNumber2,
                onValueChanged = { phoneNumber ->
                    onAction(AddEmployeeUiAction.OnPhoneNumber2Changed(phoneNumber))
                },
                isOptional = true,
                isError = error == AddEmployeeUiError.PHONE_NUMBER_IS_INVALID,
                errorMessageRes = error.messageRes,
                keyboardType = KeyboardType.Phone
            )
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun Preview() {
    ReservationManagerTheme {
        AddEmployeeScreenContent()
    }
}