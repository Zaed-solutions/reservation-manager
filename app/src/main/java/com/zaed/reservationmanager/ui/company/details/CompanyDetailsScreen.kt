package com.zaed.reservationmanager.ui.company.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.zaed.reservationmanager.data.model.CompanyBalance
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.data.model.Ride
import com.zaed.reservationmanager.ui.company.details.components.BalanceSection
import com.zaed.reservationmanager.ui.company.details.components.CompanyDetailsHeader
import com.zaed.reservationmanager.ui.company.display.components.ConfirmDeleteDialog
import com.zaed.reservationmanager.ui.reservation.details.components.RideItem
import com.zaed.reservationmanager.ui.util.PhoneUtil
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun CompanyDetailsScreen(
    modifier: Modifier = Modifier,
    viewModel: CompanyDetailsViewModel = koinViewModel(),
    companyId: String,
    isTravel: Boolean = false,
    onNavigateBack: () -> Unit,
    onNavigateToCompanyDetails: (companyId: String) -> Unit,
    onNavigateToDriverDetails: (driverId: String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    LaunchedEffect(true) {
        viewModel.init(companyId, isTravel)
    }
    CompanyDetailsScreenContent(
        modifier = modifier,
        onAction = { action ->
            when(action){
                CompanyDetailsUiAction.OnBackPressed -> onNavigateBack()
                is CompanyDetailsUiAction.OnCompanyClicked -> {
                    onNavigateToCompanyDetails(action.companyId)
                }
                is CompanyDetailsUiAction.OnDriverClicked -> {
                    onNavigateToDriverDetails(action.driverId)
                }
                is CompanyDetailsUiAction.OnCopyPhoneNumber -> {
                    clipboardManager.setText(AnnotatedString(action.phoneNumber))
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.number_copied_to_clipboard),
                            withDismissAction = true
                        )
                    }
                }
                is CompanyDetailsUiAction.OnMessagePhoneNumber -> {
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
        company = state.company,
        balance = state.balance,
        snackBarHostState = snackbarHostState,
        isTravel = isTravel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyDetailsScreenContent(
    modifier: Modifier = Modifier,
    onAction: (CompanyDetailsUiAction) -> Unit,
    company: Company = Company(),
    isTravel: Boolean = false,
    snackBarHostState: SnackbarHostState,
    rides: List<Ride> = emptyList(),
    reservation: List<Reservation> = emptyList(),
    balance: CompanyBalance = CompanyBalance(),
) {
    var isConfirmDeleteDialogVisible by remember {
        mutableStateOf(false)
    }
    var selectedRideId by remember {
        mutableStateOf("")
    }
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.company_details),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onAction(CompanyDetailsUiAction.OnBackPressed) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = null
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            CompanyDetailsHeader(
                company = company,
                onCopyPhone = { onAction(CompanyDetailsUiAction.OnCopyPhoneNumber(it)) },
                onMessagePhone = { onAction(CompanyDetailsUiAction.OnMessagePhoneNumber(it)) }
            )
            BalanceSection(
                modifier = Modifier.padding(top = 16.dp),
                balance = balance
            )
            if(isTravel){
                rides.forEach { ride ->
                    RideItem(
                        ride = ride,
                        onCompanyClicked = {companyId -> if(companyId != company.id) onAction(CompanyDetailsUiAction.OnCompanyClicked(companyId)) },
                        onDeleteRide = { isConfirmDeleteDialogVisible = true },
                        onDriverClicked = { onAction(CompanyDetailsUiAction.OnDriverClicked(it)) },
                        onCopyPhoneNumber = { onAction(CompanyDetailsUiAction.OnCopyPhoneNumber(it)) },
                        onMessagePhoneNumber = { onAction(CompanyDetailsUiAction.OnMessagePhoneNumber(it)) },
                        isActionsVisible = false
                    )
                }
                AnimatedVisibility(isConfirmDeleteDialogVisible){
                    ModalBottomSheet(
                        onDismissRequest = {
                            isConfirmDeleteDialogVisible = false
                            selectedRideId = ""
                        },
                        sheetState = rememberModalBottomSheetState()
                    ) {
                        ConfirmDeleteDialog(
                            label = stringResource(id = R.string.ride),
                            onDismiss = {
                                isConfirmDeleteDialogVisible = false
                                selectedRideId = ""
                            },
                            onConfirm = {
                                onAction(CompanyDetailsUiAction.OnDeleteRide(selectedRideId))
                                isConfirmDeleteDialogVisible = false
                                selectedRideId = ""
                            }
                        )
                    }
                }
            } else {
                reservation.forEach { reservation ->
                //todo: display list of reservations
                }
            }
        }
    }
}