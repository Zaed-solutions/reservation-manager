package com.zaed.reservationmanager.ui.reservationdetails

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.ui.reservationdetails.components.ReservationDetailsHeader
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun ReservationDetailsScreen(
    modifier: Modifier = Modifier,
    viewModel: ReservationDetailsViewModel = koinViewModel(),
    onBackPressed: () -> Unit = {},
    reservationId: String = ""
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect (true){
        viewModel.init(reservationId)
    }
    ReservationDetailsScreenContent()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReservationDetailsScreenContent(
    modifier: Modifier = Modifier,
    onAction: (ReservationDetailsUiAction) -> Unit = {},
    reservation: Reservation = Reservation(),
) {
    Scaffold (
        modifier = modifier,
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
                .verticalScroll(rememberScrollState())
        ) {
            ReservationDetailsHeader(
                modifier = Modifier.padding(top = 16.dp),
                clientName = reservation.clientName,
                onClientClicked = {
                    onAction(ReservationDetailsUiAction.OnClientClicked)
                },
                customerPhone = reservation.clientPhone,
                tourismCompany = reservation.tourismCompany,

                date = reservation.date.toString()
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