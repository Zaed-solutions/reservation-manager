package com.zaed.reservationmanager.ui.client.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.CompanyType
import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.data.model.ReservationModel
import com.zaed.reservationmanager.ui.client.details.components.CustomerDetailsHeader
import com.zaed.reservationmanager.ui.company.display.components.ConfirmDeleteDialog
import com.zaed.reservationmanager.ui.reservation.create.component.AddNewReservation
import com.zaed.reservationmanager.ui.reservation.details.components.AddRideBottomSheetContent
import com.zaed.reservationmanager.ui.reservation.details.components.ReservationsList
import com.zaed.reservationmanager.ui.util.PhoneUtil
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun CustomerDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: CustomerDetailsViewModel = koinViewModel(),
    customerId: String = "",
    onBackPressed: () -> Unit = {},
    onNavigateToCompanyDetails: (companyId: String, companyType: CompanyType) -> Unit = { _, _ -> },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    LaunchedEffect(true) {
        viewModel.init(customerId)
    }
    CustomerDetailScreenContent(
        modifier = modifier,
        customer = state.customer,
        reservations = state.reservations,
        reservationTypes = state.reservationTypes,
        cars = state.cars,
        tourismCompanies = state.tourismCompanies,
        employees = state.employees,
        travelCompanies = state.travelCompanies,
        drivers = state.drivers,
        snackbarHostState = snackbarHostState,
        onAction = { action ->
            when (action) {
                CustomerDetailsUiAction.OnBackPressed -> {
                    onBackPressed()
                }

                is CustomerDetailsUiAction.OnCompanyClicked -> {
                    onNavigateToCompanyDetails(action.companyId, CompanyType.TRAVEL)
                }

                is CustomerDetailsUiAction.OnCopyPhone -> {
                    clipboardManager.setText(AnnotatedString(action.phoneNumber))
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.number_copied_to_clipboard),
                            withDismissAction = true
                        )
                    }
                }

                is CustomerDetailsUiAction.OnMessagePhone -> {
                    if (action.phoneNumber.isNotBlank()) {
                        PhoneUtil.sendWhatsappMessage(
                            context = context,
                            phoneNumber = action.phoneNumber,
                            message = "",
                            onFailure = {
                                scope.launch {
                                    snackbarHostState.showSnackbar(context.getString(R.string.whatsapp_is_not_installed))
                                }
                            }
                        )
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar(context.getString(R.string.phonenumber_is_blank))
                        }
                    }
                }

                else -> viewModel.handleAction(action)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomerDetailScreenContent(
    modifier: Modifier = Modifier,
    customer: Customer = Customer(),
    reservations: List<ReservationModel> = emptyList(),
    reservationTypes: List<String> = emptyList(),
    cars: List<String> = emptyList(),
    tourismCompanies: List<Company> = emptyList(),
    employees: List<Employee> = emptyList(),
    travelCompanies: List<Company> = emptyList(),
    drivers: List<Employee> = emptyList(),
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
    onAction: (CustomerDetailsUiAction) -> Unit = {},
) {
    var isConfirmDeleteDialogVisible by remember {
        mutableStateOf(false)
    }
    var selectedReservation by remember {
        mutableStateOf(ReservationModel())
    }
    var isAddReservationBottomSheetVisible by remember {
        mutableStateOf(false)
    }
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.customer_details),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onAction(CustomerDetailsUiAction.OnBackPressed) }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBackIos,
                            contentDescription = null
                        )
                    }
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            CustomerDetailsHeader(
                customer = customer,
                onCopyPhone = { onAction(CustomerDetailsUiAction.OnCopyPhone(it)) },
                onMessagePhone = { onAction(CustomerDetailsUiAction.OnMessagePhone(it)) },
            )
            ReservationsList(
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                reservations = reservations,
                onCompanyClicked = { companyId ->
                    onAction(
                        CustomerDetailsUiAction.OnCompanyClicked(companyId, CompanyType.TRAVEL)
                    )
                },
                onDeleteReservation = {
                    selectedReservation = ReservationModel(id = it)
                    isConfirmDeleteDialogVisible = true
                },
                onCopyPhoneNumber = { onAction(CustomerDetailsUiAction.OnCopyPhone(it)) },
                onMessagePhoneNumber = {
                    onAction(
                        CustomerDetailsUiAction.OnMessagePhone(it)
                    )
                },
            )
            AnimatedVisibility(isAddReservationBottomSheetVisible) {
                ModalBottomSheet(
                    onDismissRequest = {
                        isAddReservationBottomSheetVisible = false
                        selectedReservation = ReservationModel()
                    },
                    sheetState = rememberModalBottomSheetState()
                ) {
                    AddRideBottomSheetContent(
                        types = reservationTypes,
                        cars = cars,
                        tourismCompanies = tourismCompanies,
                        employees = employees,
                        onFetchEmployees = {
                            onAction(
                                CustomerDetailsUiAction.OnFetchEmployees(it)
                            )
                        },
                        travelCompanies = travelCompanies,
                        drivers = drivers,
                        initialReservation = selectedReservation,
                        onFetchDrivers = {
                            onAction(
                                CustomerDetailsUiAction.OnFetchDrivers(it)
                            )
                        },
                        onAddRide = {
                            if(selectedReservation.id.isEmpty()) {
                                onAction(
                                    CustomerDetailsUiAction.OnAddReservation(it)
                                )
                            } else {
                                onAction(
                                    CustomerDetailsUiAction.OnUpdateReservation(it)
                                )
                            }
                            isAddReservationBottomSheetVisible = false
                            selectedReservation = ReservationModel()
                        },
                        onDismiss = {
                            isAddReservationBottomSheetVisible = false
                            selectedReservation = ReservationModel()
                        }
                    )
                }
            }
            AnimatedVisibility(isConfirmDeleteDialogVisible) {
                ModalBottomSheet(
                    onDismissRequest = {
                        isConfirmDeleteDialogVisible = false
                        selectedReservation = ReservationModel()
                    },
                    sheetState = rememberModalBottomSheetState()
                ) {
                    ConfirmDeleteDialog(
                        label = stringResource(
                            id = R.string.reservation
                        ),
                        onDismiss = {
                            isConfirmDeleteDialogVisible = false
                            selectedReservation = ReservationModel()
                        },
                        onConfirm = {
                            onAction(
                                CustomerDetailsUiAction.OnDeleteReservation(selectedReservation.id)
                            )
                            isConfirmDeleteDialogVisible = false
                            selectedReservation = ReservationModel()
                        }
                    )
                }
            }
        }
    }
}