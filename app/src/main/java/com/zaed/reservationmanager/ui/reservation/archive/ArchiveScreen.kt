package com.zaed.reservationmanager.ui.reservation.archive

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.ui.home.component.AddReservationBottomSheetContent
import com.zaed.reservationmanager.ui.home.component.ReservationsList
import com.zaed.reservationmanager.ui.util.PhoneUtil
import com.zaed.reservationmanager.ui.util.showSnackbarWithDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun ArchiveScreen(
    modifier: Modifier = Modifier,
    viewModel: ArchiveViewModel = koinViewModel(),
    onShowNavDrawer: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    ArchiveScreenContent(
        modifier = modifier,
        reservations = state.reservations,
        onAction = { action ->
            when (action) {
                is ArchiveUiAction.ShowNavDrawer -> {
                    onShowNavDrawer()
                }

                is ArchiveUiAction.CopyPhoneNumber -> {
                    clipboardManager.setText(AnnotatedString(action.phoneNumber))
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.number_copied_to_clipboard),
                            withDismissAction = true
                        )
                    }
                }

                is ArchiveUiAction.MessagePhoneNumber -> {
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
                }

                else -> viewModel.handleAction(action)
            }
        },
        snackBarHostState = snackbarHostState,
        reservationTypes = state.reservationTypes,
        cars = state.cars,
        tourismCompanies = state.tourismCompanies,
        employees = state.employees,
        travelCompanies = state.travelCompanies,
        drivers = state.drivers,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArchiveScreenContent(
    modifier: Modifier = Modifier,
    reservations: List<Reservation>,
    onAction: (ArchiveUiAction) -> Unit,
    reservationTypes: List<String> = emptyList(),
    cars: List<String> = emptyList(),
    tourismCompanies: List<Company> = emptyList(),
    employees: List<Employee> = emptyList(),
    travelCompanies: List<Company> = emptyList(),
    drivers: List<Employee> = emptyList(),
    snackBarHostState: SnackbarHostState,
    scope: CoroutineScope = rememberCoroutineScope(),
    context: Context = LocalContext.current
) {
    var isAddReservationBottomSheetVisible by remember {
        mutableStateOf(false)
    }
    var selectedReservation by remember {
        mutableStateOf(Reservation())
    }
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { newValue ->
            newValue != SheetValue.Hidden || !isAddReservationBottomSheetVisible
        }
    )
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.reservation_archive),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onAction(ArchiveUiAction.ShowNavDrawer)
                    }) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = null)
                    }

                },
            )
        }
    ) { innerPadding ->
        Box {
            AnimatedVisibility(isAddReservationBottomSheetVisible) {
                ModalBottomSheet(
                    onDismissRequest = {},
                    sheetState = bottomSheetState,
                    modifier = Modifier.fillMaxSize(),
                    properties = ModalBottomSheetProperties(shouldDismissOnBackPress = false)

                ) {
                    AddReservationBottomSheetContent(
                        modifier = Modifier.fillMaxSize(),
                        types = reservationTypes,
                        cars = cars,
                        tourismCompanies = tourismCompanies,
                        onFetchEmployees = {
                            onAction(ArchiveUiAction.OnFetchEmployees(it))
                        },
                        employees = employees,
                        travelCompanies = travelCompanies,
                        onFetchDrivers = {
                            onAction(ArchiveUiAction.OnFetchDrivers(it))
                        },
                        drivers = drivers,
                        initialReservation = selectedReservation,
                        onSaveReservation = {
                            isAddReservationBottomSheetVisible = false
                            onAction(
                                if (selectedReservation.id.isNotBlank()) {
                                    ArchiveUiAction.OnEditReservation(
                                        reservation = it,
                                        onSuccess = {
                                            snackBarHostState.showSnackbarWithDuration(
                                                message = context.getString(R.string.reservation_updated_successfully),
                                                durationMillis = 1500L,
                                                scope = scope,
                                                onFinished = {
                                                    selectedReservation = Reservation()
                                                }
                                            )
                                        }
                                    )
                                } else {
                                    ArchiveUiAction.OnAddReservation(
                                        reservation = it,
                                        onSuccess = {
                                            snackBarHostState.showSnackbarWithDuration(
                                                message = context.getString(R.string.reservation_added_successfully),
                                                durationMillis = 1500L,
                                                scope = scope,
                                                onFinished = {
                                                    selectedReservation = Reservation()
                                                }
                                            )
                                        }
                                    )
                                }
                            )
                        },
                        onDismiss = {
                            isAddReservationBottomSheetVisible = false
                            selectedReservation = Reservation()
                        }

                    )
                }
            }
        }
        ReservationsList(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            reservations = reservations,
            isEditProfileEnabled = false,
            onArchiveReservation = { reservationId ->
                onAction(ArchiveUiAction.UnarchiveReservation(reservationId))
            },
            isHeaderVisible = false,
            isAddEnabled = false,
            isSendActionsVisible = false,
            isEditable = false,
            onDeleteReservation = { reservation ->
                onAction(ArchiveUiAction.DeleteReservation(reservation))
            },
            onCopyPhoneNumber = { phoneNumber ->
                onAction(ArchiveUiAction.CopyPhoneNumber(phoneNumber))
            },
            onMessagePhoneNumber = { phoneNumber ->
                onAction(ArchiveUiAction.MessagePhoneNumber(phoneNumber))
            },
            onAddSecondaryReservation = { mainReservation ->
                selectedReservation =
                    Reservation(
                        reservationNumber = mainReservation.reservationNumber,
                        mainReservation = false,
                        mainReservationId = mainReservation.id,
                        tourismCompany = mainReservation.tourismCompany,
                        tourismCompanyId = mainReservation.tourismCompanyId,
                        tourismCompanyPhone = mainReservation.tourismCompanyPhone,
                        tourismEmployeeId = mainReservation.tourismEmployeeId,
                        tourismEmployee = mainReservation.tourismEmployee,
                        tourismEmployeePhone = mainReservation.tourismEmployeePhone,
                        flightNumber = mainReservation.flightNumber,
                        clientId = mainReservation.clientId,
                        clientName = mainReservation.clientName,
                        clientPhone = mainReservation.clientPhone,
                        clientCountry = mainReservation.clientCountry,
                    )
                isAddReservationBottomSheetVisible = true
            }
        )
    }
}