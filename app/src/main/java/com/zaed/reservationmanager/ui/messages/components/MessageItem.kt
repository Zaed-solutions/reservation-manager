package com.zaed.reservationmanager.ui.messages.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Message

@Composable
fun MessageItem(
    modifier: Modifier = Modifier,
    message: Message,
    onCopyMessage: () -> Unit,
    onEditMessage: () -> Unit,
    onDeleteMessage: () -> Unit,
) {
    var isExpanded by remember {
        mutableStateOf(false)
    }
    var isOptionMenuVisible by remember {
        mutableStateOf(false)
    }
    val anim = remember {
        Animatable(0f)
    }
    LaunchedEffect(isExpanded) {
        anim.animateTo(
            targetValue = if (isExpanded) 180f else 0f
        )
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        onClick = { isExpanded = !isExpanded },
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = message.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        onCopyMessage()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null
                    )
                }
                Box(
                    modifier = Modifier
                        .wrapContentSize(Alignment.TopEnd)
                ) {
                    IconButton(
                        onClick = { isOptionMenuVisible = !isOptionMenuVisible },
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(24.dp)

                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = null,
                        )
                    }

                    DropdownMenu(
                        expanded = isOptionMenuVisible,
                        onDismissRequest = { isOptionMenuVisible = false }
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                onEditMessage()
                                isOptionMenuVisible = false
                            },
                            text = {
                                Text(
                                    text = stringResource(R.string.edit),
                                )
                            },
                        )
                        DropdownMenuItem(
                            onClick = {
                                onDeleteMessage()
                                isOptionMenuVisible = false
                            },
                            text = {
                                Text(
                                    text = stringResource(R.string.delete),
                                )
                            },
                        )
                    }
                }
            }
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = message.message,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.rotate(anim.value)
            )
        }
    }
}