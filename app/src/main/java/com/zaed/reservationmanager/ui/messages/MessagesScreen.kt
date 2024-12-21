package com.zaed.reservationmanager.ui.messages

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import com.zaed.reservationmanager.data.model.Message
import com.zaed.reservationmanager.ui.company.display.components.ConfirmDeleteDialog
import com.zaed.reservationmanager.ui.home.HomeUiAction
import com.zaed.reservationmanager.ui.messages.components.AddMessageBottomSheetContent
import com.zaed.reservationmanager.ui.messages.components.MessagesList
import com.zaed.reservationmanager.ui.reservation.archive.ArchiveUiAction
import com.zaed.reservationmanager.ui.util.showSnackbarWithDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun MessagesScreen(
    modifier: Modifier = Modifier,
    viewModel: MessagesViewModel = koinViewModel(),
    onShowNavDrawer: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    MessagesScreenContent(
        modifier = modifier,
        messages = state.displayedMessages,
        searchQuery = state.searchQuery,
        snackbarHostState = snackbarHostState,
        scope = scope,
        context = context,
        onAction = { action ->
            when(action){
                is MessagesUiAction.OnCopyMessage -> {
                    clipboardManager.setText(AnnotatedString(action.message))
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.message_copied_to_clipboard),
                            withDismissAction = true
                        )
                    }
                }
                MessagesUiAction.OnShowNavDrawer -> onShowNavDrawer()
                else -> viewModel.handleAction(action)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessagesScreenContent(
    modifier: Modifier = Modifier,
    messages: List<Message>,
    searchQuery: String,
    onAction: (MessagesUiAction) -> Unit,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope = rememberCoroutineScope(),
    context: Context = LocalContext.current
) {

    var isAddMessageBottomSheetVisible by remember {
        mutableStateOf(false)
    }
    var isConfirmDeleteBottomSheetVisible by remember {
        mutableStateOf(false)
    }
    var selectedMessage by remember{
        mutableStateOf(Message())
    }
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.messages),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onAction(MessagesUiAction.OnShowNavDrawer)
                    }) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = null)
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedMessage = Message()
                    isAddMessageBottomSheetVisible = true
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
            }
        }
    ) { innerPadding ->
        Column (
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                value = searchQuery,
                onValueChange = {
                    onAction(MessagesUiAction.OnSearchMessages(it))
                },
                placeholder = { Text(stringResource(R.string.search)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                onAction(MessagesUiAction.OnSearchMessages(""))
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = null
                            )
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.large
            )
            MessagesList(
                messages = messages,
                onCopyMessage = {
                    onAction(MessagesUiAction.OnCopyMessage(it))
                },
                onEditMessage = {
                    selectedMessage = it
                    isAddMessageBottomSheetVisible = true
                },
                onDeleteMessage = {
                    selectedMessage = it
                    isConfirmDeleteBottomSheetVisible = true
                }
            )
            AnimatedVisibility(isAddMessageBottomSheetVisible) {
                ModalBottomSheet(
                    onDismissRequest = {
                        isConfirmDeleteBottomSheetVisible = false
                        selectedMessage = Message()
                    }
                ) {
                    AddMessageBottomSheetContent(
                        initialMessage = selectedMessage,
                        onSubmit = { message ->
                            isAddMessageBottomSheetVisible = false
                            if (message.id.isNotBlank()) {
                                onAction(
                                    MessagesUiAction.OnEditMessage(
                                        message,
                                        onSuccess = {
                                            snackbarHostState.showSnackbarWithDuration(
                                                message = context.getString(R.string.message_updated_successfully),
                                                durationMillis = 1500L,
                                                scope = scope,
                                                onFinished = {
                                                    selectedMessage = Message()
                                                }
                                            )
                                        }
                                    )
                                )
                            } else {
                                onAction(
                                    MessagesUiAction.OnAddMessage(
                                        message,
                                        onSuccess = {
                                            snackbarHostState.showSnackbarWithDuration(
                                                message = context.getString(R.string.message_added_successfully),
                                                durationMillis = 1500L,
                                                scope = scope,
                                                onFinished = {
                                                    selectedMessage = Message()
                                                }
                                            )
                                        }
                                    )
                                )
                            }
                        }
                    )
                }
            }
            AnimatedVisibility(isConfirmDeleteBottomSheetVisible) {
                ModalBottomSheet(
                    onDismissRequest = {
                        isConfirmDeleteBottomSheetVisible = false
                        selectedMessage = Message()
                    }
                ) {
                    ConfirmDeleteDialog(
                        label = stringResource(R.string.message),
                        onDismiss = {
                            isConfirmDeleteBottomSheetVisible = false
                            selectedMessage = Message()
                        },
                        onConfirm = {
                            onAction(MessagesUiAction.OnDeleteMessage(selectedMessage.id))
                            isConfirmDeleteBottomSheetVisible = false
                            selectedMessage = Message()
                        }
                    )
                }
            }
        }
    }

}