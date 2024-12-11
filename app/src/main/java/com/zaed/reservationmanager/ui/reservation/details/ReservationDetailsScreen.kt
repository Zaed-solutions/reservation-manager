package com.zaed.reservationmanager.ui.reservation.details

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.data.model.Ride
import com.zaed.reservationmanager.ui.reservation.details.components.ReservationDetailsHeader
import com.zaed.reservationmanager.ui.reservation.details.components.RidesList
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun ReservationDetailsScreen(
    modifier: Modifier = Modifier,
    viewModel: ReservationDetailsViewModel = koinViewModel(),
    onBackPressed: () -> Unit = {},
    reservationId: String = "",
    onNavigateToClientDetails: (String) -> Unit = {},
    onNavigateToCompanyDetails: (String, Boolean) -> Unit = {_,_ ->},
    onNavigateToEmployeeDetails: (String, Boolean) -> Unit = {_,_ ->},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    LaunchedEffect (true){
        viewModel.init(reservationId)
    }
    ReservationDetailsScreenContent(
        modifier = modifier,
        reservation = state.reservation,
        rides = state.rides,
        onAction = { action ->
            when(action){
                ReservationDetailsUiAction.OnBackPressed -> {
                    onBackPressed()
                }
                is ReservationDetailsUiAction.OnClientClicked -> {
                    onNavigateToClientDetails(action.clientId)
                }
                is ReservationDetailsUiAction.OnCompanyClicked -> {
                    onNavigateToCompanyDetails(action.companyId, action.isTravel)
                }
                is ReservationDetailsUiAction.OnCopyPhoneNumber -> {
                    clipboardManager.setText(AnnotatedString(action.phoneNumber))
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.phone_number_copied_to_clipboard),
                            withDismissAction = true
                        )
                    }
                }
                is ReservationDetailsUiAction.OnEmployeeClicked -> {
                    onNavigateToEmployeeDetails(action.employeeId, action.isDriver)
                }
                is ReservationDetailsUiAction.OnMessagePhoneNumber -> {
                    val whatsappIntent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://wa.me/${action.phoneNumber}")
                    }
                    context.startActivity(whatsappIntent)
                }
                ReservationDetailsUiAction.OnSendConfirmationMessage -> {
                    val whatsappIntent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://wa.me/${state.reservation.clientPhone}")
                    }
                    //todo: add message content
                    context.startActivity(whatsappIntent)
                }
                is ReservationDetailsUiAction.OnSendDriverInfoToCustomer -> {
                    val whatsappIntent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://wa.me/${state.reservation.clientPhone}")
                    }
                    //todo: add message content
                    context.startActivity(whatsappIntent)
                }
                is ReservationDetailsUiAction.OnSendInfoToTravelCompany -> {
                    val whatsappIntent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://wa.me/${action.ride.travelCompanyPhone}")
                    }
                    //todo: add message content
                    context.startActivity(whatsappIntent)
                }
                else -> viewModel.handleAction(action)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReservationDetailsScreenContent(
    modifier: Modifier = Modifier,
    onAction: (ReservationDetailsUiAction) -> Unit = {},
    reservation: Reservation = Reservation(),
    rides: List<Ride> = emptyList(),
    snackbarHostState: SnackbarHostState = remember{ SnackbarHostState() }
) {
    Scaffold (
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.reservation_details),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onAction(ReservationDetailsUiAction.OnBackPressed) }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ){ innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
//                .verticalScroll(rememberScrollState())
        ) {
            ReservationDetailsHeader(
                modifier = Modifier.padding(top = 16.dp),
                reservation = reservation,
                onClientClicked = {
                    onAction(ReservationDetailsUiAction.OnClientClicked(reservation.clientId))
                },
                onCopyPhone = { phoneNumber ->
                    onAction(ReservationDetailsUiAction.OnCopyPhoneNumber(phoneNumber))
                },
                onMessagePhone = { phoneNumber ->
                    onAction(ReservationDetailsUiAction.OnMessagePhoneNumber(phoneNumber))
                },
                onTourismEmployeeClicked = {
                    onAction(ReservationDetailsUiAction.OnEmployeeClicked(reservation.tourismEmployeeId))
                },
                onTourismCompanyClicked = {
                    onAction(ReservationDetailsUiAction.OnCompanyClicked(reservation.tourismCompanyId))
                },
                onSendConfirmationMessage = {
                    onAction(ReservationDetailsUiAction.OnSendConfirmationMessage)
                }
            )
            RidesList(
                modifier = Modifier.padding(top = 16.dp),
                rides = rides,
                onAddRide = {/*TODO*/},
                onDriverClicked = { driverId ->
                    onAction(ReservationDetailsUiAction.OnEmployeeClicked(driverId, true))
                },
                onCopyPhoneNumber = { phoneNumber ->
                    onAction(ReservationDetailsUiAction.OnCopyPhoneNumber(phoneNumber))
                },
                onMessagePhoneNumber = { phoneNumber ->
                    onAction(ReservationDetailsUiAction.OnMessagePhoneNumber(phoneNumber))
                },
                onDeleteRide = {/*TODO*/},
                onCompanyClicked = { companyId ->
                    onAction(ReservationDetailsUiAction.OnCompanyClicked(companyId, true))
                },
                onSendInfoToTravelCompany = { ride ->
                    onAction(ReservationDetailsUiAction.OnSendInfoToTravelCompany(ride))
                },
                onSendDriverInfoToClient = {driverName, driverPhoneNumber ->
                    onAction(ReservationDetailsUiAction.OnSendDriverInfoToCustomer(driverName, driverPhoneNumber))
                },
            )

        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun Preview() {
    ReservationManagerTheme {
        ReservationDetailsScreenContent()
    }
}