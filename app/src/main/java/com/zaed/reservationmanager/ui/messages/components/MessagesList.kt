package com.zaed.reservationmanager.ui.messages.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Message

@Composable
fun MessagesList(
    modifier: Modifier = Modifier,
    messages: List<Message>,
    searchQuery: String = "",
    onCopyMessage: (String) -> Unit,
    onEditMessage: (Message) -> Unit,
    onDeleteMessage: (Message) -> Unit,
) {
    AnimatedContent(targetState = messages.isEmpty() to searchQuery.isBlank()) { state ->
        when {
            state.first && state.second -> {
                Column (
                    modifier = modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text(
                        modifier = Modifier.padding(top = 36.dp),
                        text = stringResource(R.string.no_messages_added),
                        style = MaterialTheme.typography.titleLarge
                    )
                }

            }
            state.first && !state.second -> {
                Column (
                    modifier = modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text(
                        modifier = Modifier.padding(top = 36.dp),
                        text = stringResource(R.string.no_matching_messages),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = modifier,
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(messages) { message ->
                        MessageItem(
                            message = message,
                            onCopyMessage = {
                                onCopyMessage(message.message)
                            },
                            onDeleteMessage = {
                                onDeleteMessage(message)
                            },
                            onEditMessage = {
                                onEditMessage(message)
                            }
                        )
                    }
                }
            }
        }
    }
}