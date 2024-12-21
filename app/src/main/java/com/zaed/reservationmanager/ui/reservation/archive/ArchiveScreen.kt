package com.zaed.reservationmanager.ui.reservation.archive

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.ui.home.HomeUiAction
import com.zaed.reservationmanager.ui.home.component.ReservationsList
import com.zaed.reservationmanager.ui.util.PhoneUtil
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
            when(action){
                is ArchiveUiAction.ShowNavDrawer ->{
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
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArchiveScreenContent(
    modifier: Modifier = Modifier,
    reservations: List<Reservation>,
    onAction: (ArchiveUiAction) -> Unit
) {
    Scaffold (
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
    ){ innerPadding ->
        ReservationsList(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            reservations = reservations,
            onArchiveReservation = { reservationId ->
                onAction(ArchiveUiAction.UnarchiveReservation(reservationId))
            },
            isHeaderVisible = false,
            isAddEnabled = false,
            isSendActionsVisible = false,
            isEditable = false,
            onDeleteReservation = { reservationId ->
                onAction(ArchiveUiAction.DeleteReservation(reservationId))
            },
            onCopyPhoneNumber = { phoneNumber ->
                onAction(ArchiveUiAction.CopyPhoneNumber(phoneNumber))
            },
            onMessagePhoneNumber = { phoneNumber ->
                onAction(ArchiveUiAction.MessagePhoneNumber(phoneNumber))
            },
        )
    }
}