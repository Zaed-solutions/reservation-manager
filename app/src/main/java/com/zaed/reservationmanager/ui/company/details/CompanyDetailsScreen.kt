package com.zaed.reservationmanager.ui.company.details

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.CompanyBalance
import com.zaed.reservationmanager.data.model.CompanyType
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.data.model.Ride
import com.zaed.reservationmanager.ui.company.details.components.BalanceSection
import com.zaed.reservationmanager.ui.company.details.components.CompanyDetailsHeader
import com.zaed.reservationmanager.ui.company.display.components.ConfirmDeleteDialog
import com.zaed.reservationmanager.ui.reservation.details.components.RideItem
import com.zaed.reservationmanager.ui.reservation.display.component.ExpandableReservationCard
import com.zaed.reservationmanager.ui.reservation.details.components.RidesList
import com.zaed.reservationmanager.ui.reservation.display.component.ReservationList
import com.zaed.reservationmanager.ui.util.PhoneUtil
import com.zaed.reservationmanager.ui.util.SheetUtil.exportRidesAsCsv
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun CompanyDetailsScreen(
    modifier: Modifier = Modifier,
    viewModel: CompanyDetailsViewModel = koinViewModel(),
    companyId: String,
    companyType: CompanyType = CompanyType.TOURISM,
    onNavigateBack: () -> Unit,
    onNavigateToCompanyDetails: (companyId: String, companyType: CompanyType) -> Unit,
    onNavigateToDriverDetails: (driverId: String) -> Unit,
    onNavigateToEditReservation: (Reservation) -> Unit,
    onNavigateToReservationDetails: (reservationId: String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    LaunchedEffect(true) {
        viewModel.init(companyId, companyType)
    }
    CompanyDetailsScreenContent(
        modifier = modifier,
        onAction = { action ->
            when (action) {
                CompanyDetailsUiAction.OnBackPressed -> onNavigateBack()
                is CompanyDetailsUiAction.ExportRidesAsCSV -> {
                    val headers = if (companyType == CompanyType.TRAVEL) {
                        listOf(
                            context.getString(R.string.date),
                            context.getString(R.string.type),
                            context.getString(R.string.car),
                            context.getString(R.string.client_name),
                            context.getString(R.string.buying_price),
                            context.getString(R.string.collected_price),
                            context.getString(R.string.balance),
                        )
                    } else if (companyType == CompanyType.TOURISM) {
                        listOf(
                            context.getString(R.string.date),
                            context.getString(R.string.type),
                            context.getString(R.string.car),
                            context.getString(R.string.client_name),
                            context.getString(R.string.selling_price),
                            context.getString(R.string.collected_price),
                            context.getString(R.string.balance),
                        )
                    } else {
                        listOf(
                            context.getString(R.string.date),
                            context.getString(R.string.type),
                            context.getString(R.string.car),
                            context.getString(R.string.client_name),
                            context.getString(R.string.selling_price),
                            context.getString(R.string.buying_price),
                            context.getString(R.string.collected_price),
                            context.getString(R.string.balance),
                        )
                    }
                    val file = state.rides.exportRidesAsCsv(
                        context = context,
                        headers = headers,
                        isTravelCompany = companyType == CompanyType.TRAVEL,
                        isTourismCompany = companyType == CompanyType.TOURISM,
                        isAllRides = companyType == CompanyType.TRAVEL_AND_TOURISM
                    )
                    scope.launch {
                        if (file != null) {
                            snackbarHostState.showSnackbar(
                                message = context.getString(R.string.csv_saved_at, file.path),
                                actionLabel = context.getString(R.string.open)
                            ).let { result ->
                                if (result == SnackbarResult.ActionPerformed) {
                                    try {
                                        val openFileIntent = Intent(Intent.ACTION_VIEW).apply {
                                            val fileUri: Uri = FileProvider.getUriForFile(
                                                context,
                                                "${context.packageName}.fileprovider",
                                                file
                                            )
                                            setDataAndType(fileUri, "text/csv")
                                            flags =
                                                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        if (openFileIntent.resolveActivity(context.packageManager) != null) {
                                            context.startActivity(openFileIntent)
                                        } else {
                                            snackbarHostState.showSnackbar(context.getString(R.string.no_csv_viewer_found))
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        } else {
                            snackbarHostState.showSnackbar(context.getString(R.string.error_exporting_csv))
                        }
                    }
                }

                is CompanyDetailsUiAction.OnCompanyClicked -> {
                    onNavigateToCompanyDetails(action.companyId, action.type)
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

                is CompanyDetailsUiAction.OnEditReservation -> {
                    onNavigateToEditReservation(action.reservation)
                }

                is CompanyDetailsUiAction.OnReservationClicked -> {
                    onNavigateToReservationDetails(action.reservationId)
                }

                else -> viewModel.handleAction(action)
            }
        },
        company = state.company,
        balance = state.balance,
        rides = state.rides,
        reservations = state.reservations,
        snackBarHostState = snackbarHostState,
        companyType = companyType
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyDetailsScreenContent(
    modifier: Modifier = Modifier,
    onAction: (CompanyDetailsUiAction) -> Unit,
    company: Company = Company(),
    companyType: CompanyType = CompanyType.TOURISM,
    snackBarHostState: SnackbarHostState,
    rides: List<Ride> = emptyList(),
    reservations: List<Reservation> = emptyList(),
    balance: CompanyBalance = CompanyBalance(),
) {
    var isConfirmDeleteDialogVisible by remember {
        mutableStateOf(false)
    }
    var isRideSelected by remember {
        mutableStateOf(false)
    }
    var selectedItemId by remember {
        mutableStateOf("")
    }
    var isOptionsMenuVisible by remember {
        mutableStateOf(false)
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
                            imageVector = Icons.AutoMirrored.Default.ArrowBackIos,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .wrapContentSize(Alignment.TopEnd)
                    ) {
                        IconButton(
                            onClick = { isOptionsMenuVisible = !isOptionsMenuVisible },
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = null,
                            )
                        }
                        DropdownMenu(
                            expanded = isOptionsMenuVisible,
                            onDismissRequest = { isOptionsMenuVisible = false }
                        ) {
                            DropdownMenuItem(
                                onClick = {
                                    onAction(CompanyDetailsUiAction.ExportRidesAsCSV)
                                    isOptionsMenuVisible = false
                                },
                                text = {
                                    Text(
                                        text = stringResource(R.string.export_as_csv),
                                    )
                                },
                            )
//                            DropdownMenuItem(
//                                onClick = {
//                                    onExportCustomersAsPDF()
//                                    isOptionsMenuVisible = false
//                                },
//                                text = {
//                                    Text(
//                                        text = stringResource(R.string.export_as_pdf),
//                                    )
//                                },
//                            )
                        }
                    }
                }
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
            if (companyType == CompanyType.TRAVEL) {
                RidesList(
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                    rides = rides,
                    isAddEnabled = false,
                    isSendActionsVisible = false,
                    onCompanyClicked = { companyId ->
                        if (companyId != company.id) onAction(
                            CompanyDetailsUiAction.OnCompanyClicked(companyId, CompanyType.TRAVEL)
                        )
                    },
                    onDeleteRide = {
                        isRideSelected = true
                        selectedItemId = it
                        isConfirmDeleteDialogVisible = true
                    },
                    onDriverClicked = { onAction(CompanyDetailsUiAction.OnDriverClicked(it)) },
                    onCopyPhoneNumber = { onAction(CompanyDetailsUiAction.OnCopyPhoneNumber(it)) },
                    onMessagePhoneNumber = {
                        onAction(
                            CompanyDetailsUiAction.OnMessagePhoneNumber(
                                it
                            )
                        )
                    },
                )
            } else if (companyType == CompanyType.TOURISM) {
                Text(
                    text = stringResource(id = R.string.reservations),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                ReservationList(
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                    reservations = reservations,
                    onDeleteReservation = { reservationId ->
                        isRideSelected = false
                        selectedItemId = reservationId
                        isConfirmDeleteDialogVisible = true
                    },
                    onNavigateToEditReservation = {
                        onAction(
                            CompanyDetailsUiAction.OnEditReservation(
                                it
                            )
                        )
                    },
                    onNavigateToReservationDetails = { reservationId ->
                        onAction(
                            CompanyDetailsUiAction.OnReservationClicked(
                                reservationId
                            )
                        )
                    },
                )
            } else {
                var selectedTabIndex by remember {
                    mutableStateOf(0)
                }
                PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                    listOf(
                        stringResource(R.string.reservations),
                        stringResource(R.string.rides)
                    ).forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }
                if (selectedTabIndex == 0) {
                    ReservationList(
                        reservations = reservations,
                        onNavigateToReservationDetails = {
                            onAction(CompanyDetailsUiAction.OnReservationClicked(it))
                        },
                        onDeleteReservation = {
                            isRideSelected = false
                            selectedItemId = it
                            isConfirmDeleteDialogVisible = true
                        },
                        onNavigateToEditReservation = {
                            onAction(CompanyDetailsUiAction.OnEditReservation(it))
                        }
                    )
                } else {
                    RidesList(
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                        rides = rides,
                        isHeaderVisible = false,
                        isAddEnabled = false,
                        isSendActionsVisible = false,
                        onCompanyClicked = { companyId ->
                            if (companyId != company.id) onAction(
                                CompanyDetailsUiAction.OnCompanyClicked(companyId, CompanyType.TRAVEL)
                            )
                        },
                        onDeleteRide = {
                            isRideSelected = true
                            selectedItemId = it
                            isConfirmDeleteDialogVisible = true
                        },
                        onDriverClicked = { onAction(CompanyDetailsUiAction.OnDriverClicked(it)) },
                        onCopyPhoneNumber = { onAction(CompanyDetailsUiAction.OnCopyPhoneNumber(it)) },
                        onMessagePhoneNumber = {
                            onAction(
                                CompanyDetailsUiAction.OnMessagePhoneNumber(
                                    it
                                )
                            )
                        },
                    )
                }
            }
            AnimatedVisibility(isConfirmDeleteDialogVisible) {
                ModalBottomSheet(
                    onDismissRequest = {
                        isConfirmDeleteDialogVisible = false
                        selectedItemId = ""
                    },
                    sheetState = rememberModalBottomSheetState()
                ) {
                    ConfirmDeleteDialog(
                        label = if (isRideSelected) stringResource(id = R.string.ride) else stringResource(
                            id = R.string.reservation
                        ),
                        onDismiss = {
                            isConfirmDeleteDialogVisible = false
                            selectedItemId = ""
                        },
                        onConfirm = {
                            onAction(
                                if (isRideSelected)
                                    CompanyDetailsUiAction.OnDeleteRide(selectedItemId)
                                else
                                    CompanyDetailsUiAction.OnDeleteReservation(selectedItemId)
                            )
                            isConfirmDeleteDialogVisible = false
                            selectedItemId = ""
                        }
                    )
                }
            }
        }
    }
}