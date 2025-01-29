package com.zaed.reservationmanager.ui.reservation.create

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.ui.home.component.AddReservationBottomSheetContent
import com.zaed.reservationmanager.ui.reservation.create.component.AddedReservationsList
import com.zaed.reservationmanager.ui.reservation.create.component.CenterAlignedTopBar
import com.zaed.reservationmanager.ui.reservation.create.component.CustomerInfoSection
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme
import com.zaed.reservationmanager.ui.util.showSnackbarWithDuration
import org.koin.androidx.compose.koinViewModel

@Composable
fun CreateReservationScreen(
    viewModel: CreateReservationViewModel = koinViewModel(),
    navigateBack: () -> Unit,
    navigateToCustomerDetailsScreen : (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    LaunchedEffect(state.isFinished) {
        if (state.isFinished) {
            snackbarHostState.showSnackbarWithDuration(
                message = if(state.isNewCustomer == false)
                    context.getString(R.string.reservation_added_successfully)
                else
                    context.getString(R.string.customer_added_successfully),
                durationMillis = 1500L,
                scope = scope,
                onFinished = {
                    navigateToCustomerDetailsScreen(state.customer.id)
                }
            )
        }
    }
    LaunchedEffect (state.isNewCustomer){
        if(state.isNewCustomer == false){
            navigateToCustomerDetailsScreen(state.customer.id)
        }
    }
    CreateReservationScreenContent(
        snackbarHostState = snackbarHostState,
        reservations = state.reservations,
        isNewCustomer = state.isNewCustomer,
        customer = state.customer,
        tourismCompanies = state.tourismCompanies,
        employees = state.employees,
        travelCompanies = state.travelCompanies,
        drivers = state.drivers,
        reservationTypes = state.reservationTypes,
        cars = state.carTypes,
        countries = state.countries,
        isLoading = state.isLoading,
        reservationError = state.reservationError,
        onAction = { action ->
            when (action) {
                CreateReservationUiAction.OnBackPressed -> navigateBack()
                else -> viewModel.handleAction(action)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateReservationScreenContent(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    reservations: List<Reservation> = emptyList(),
    isNewCustomer: Boolean? = null,
    customer: Customer = Customer(),
    tourismCompanies: List<Company> = emptyList(),
    employees: List<Employee> = emptyList(),
    travelCompanies: List<Company> = emptyList(),
    drivers: List<Employee> = emptyList(),
    reservationTypes: List<String> = emptyList(),
    cars: List<String> = emptyList(),
    countries: List<String> = emptyList(),
    isLoading: Boolean = false,
    reservationError: ReservationError = ReservationError.NONE,
    onAction: (CreateReservationUiAction) -> Unit,
) {
    var isAddReservationSheetVisible by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { newValue ->
            newValue != SheetValue.Hidden || !isAddReservationSheetVisible
        }
    )
    val nestedScrollConnection = rememberNestedScrollInteropConnection()
    Scaffold(
        modifier = modifier,
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        topBar = {
            CenterAlignedTopBar(
                onBackClicked = { onAction(CreateReservationUiAction.OnBackPressed) },
                title = stringResource(R.string.create_new_reservation)
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
                    enabled = isNewCustomer != null && customer.isComplete() && (isNewCustomer || reservations.isNotEmpty()),
                    onClick = { onAction(CreateReservationUiAction.SaveReservations) },
                ) {
                    Text(text = if(isNewCustomer == false) stringResource(R.string.save_reservations) else stringResource(R.string.save_customer))
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
            .nestedScroll(nestedScrollConnection),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isLoading) {
                LinearProgressIndicator()
            }
            CustomerInfoSection(
                customer = customer,
                isNewCustomer = isNewCustomer,
                onSearchCustomer = {
                    onAction(CreateReservationUiAction.SearchCustomer)
                },
                onUpdateName = {
                    onAction(CreateReservationUiAction.UpdateCustomerName(it))
                },
                onUpdateCountry = {
                    onAction(CreateReservationUiAction.UpdateCustomerCountry(it))
                },
                onUpdateCity = {
                    onAction(CreateReservationUiAction.UpdateCustomerCity(it))
                },
                countries = countries,
                onUpdatePhoneNumber = {
                    onAction(CreateReservationUiAction.UpdateCustomerPhone(it))
                },
                onUpdatePhoneNumber2 = {
                    onAction(CreateReservationUiAction.UpdateCustomerPhone2(it))
                },
                onUpdateEmail = {
                    onAction(CreateReservationUiAction.UpdateCustomerEmail(it))
                },
                error = reservationError,
                onUpdateNationality = {
                    onAction(CreateReservationUiAction.UpdateCustomerNationality(it))
                },
                onUpdateJob = {
                    onAction(CreateReservationUiAction.UpdateCustomerJob(it))
                }
            )
            AnimatedVisibility(isNewCustomer == false) {
                AddedReservationsList(
                    reservations = reservations,
                    onAddReservation = {
                        isAddReservationSheetVisible = true
                    },
                    isSendActionsVisible = false,
                    onDeleteReservation = {
                        onAction(CreateReservationUiAction.DeleteReservation(it))
                    },
                )
            }
            AnimatedVisibility(isAddReservationSheetVisible) {
                ModalBottomSheet(
                    modifier = Modifier.fillMaxSize(),
                    sheetState = bottomSheetState,
                    onDismissRequest = {},
                    properties = ModalBottomSheetProperties(shouldDismissOnBackPress = false)
                ) {
                    AddReservationBottomSheetContent(
                        modifier = Modifier.fillMaxSize(),
                        tourismCompanies = tourismCompanies,
                        employees = employees,
                        onFetchEmployees = {
                            onAction(CreateReservationUiAction.FetchEmployees(it))
                        },
                        types = reservationTypes,
                        cars = cars,
                        travelCompanies = travelCompanies,
                        onFetchDrivers = {
                            onAction(CreateReservationUiAction.FetchDrivers(it))
                        },
                        drivers = drivers,
                        onSaveReservation = {
                            onAction(CreateReservationUiAction.AddReservation(it))
                            isAddReservationSheetVisible = false
                        },
                        onDismiss = {
                            isAddReservationSheetVisible = false
                        }
                    )
                }
            }
        }
    }
}


@Preview()
@Composable
fun NewClientDataEntryScreenPreview() {
    ReservationManagerTheme {
//        CreateReservationScreenContent(
//            reservationModels = listOf(
//                ReservationModel(
//                )
//            )
//        )
    }
}