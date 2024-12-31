package com.zaed.reservationmanager.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme
import kotlin.Int.Companion.MAX_VALUE

@Composable
fun TitledTextField(
    modifier: Modifier = Modifier,
    title: String = "",
    initialValue: String = "",
    onValueChanged: (String) -> Unit = {},
    isOptional: Boolean = true,
    isError: Boolean = false,
    errorMessageRes: Int = 0,
    keyboardType: KeyboardType = KeyboardType.Unspecified,
    imeAction: ImeAction = ImeAction.Default,
    isEnabled: Boolean = true,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isReadOnly: Boolean = false,
    singleLine: Boolean = true,
) {
    var value by remember { mutableStateOf(initialValue) }
    Column(
        modifier = modifier
            .widthIn(max = 400.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title + if (isOptional) "" else " *",
            style = MaterialTheme.typography.titleMedium,
        )
        OutlinedTextField(
            singleLine = singleLine,
            value = value,
            enabled = isEnabled,
            onValueChange = {
                value = if(keyboardType == KeyboardType.Phone) it.replace(" ", "") else it
                onValueChanged(value)
            },
            readOnly = isReadOnly,
            shape = MaterialTheme.shapes.small,
            isError = isError,
            supportingText = {
                if (isError) {
                    Text(
                        text = stringResource(id = errorMessageRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = keyboardActions,
            modifier = Modifier
                .fillMaxWidth(),
            trailingIcon = {
                AnimatedVisibility(visible = value.isNotBlank()) {
                    IconButton(onClick = {
                        onValueChanged("")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            tint = MaterialTheme.colorScheme.secondary,
                            contentDescription = null
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun TitledTextField2(
    modifier: Modifier = Modifier,
    title: String = "",
    value: String = "",
    onValueChanged: (String) -> Unit = {},
    isOptional: Boolean = true,
    isError: Boolean = false,
    maxLines: Int = 1,
    errorMessageRes: Int = 0,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    isEnabled: Boolean = true,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isReadOnly: Boolean = false,
) {
    Column(
        modifier = modifier
            .widthIn(max = 400.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title + if (isOptional) "" else " *",
            style = MaterialTheme.typography.titleMedium,
        )
        OutlinedTextField(
            singleLine = maxLines == 1,
            value = if(keyboardType == KeyboardType.Number && value==0.toString()) "" else value,
            enabled = isEnabled,
            onValueChange = {
                onValueChanged(if(keyboardType == KeyboardType.Phone) it.replace(" ", "") else it)
            },
            maxLines=maxLines,
            readOnly = isReadOnly,
            shape = MaterialTheme.shapes.small,
            isError = isError,
            supportingText = {
                if (isError) {
                    Text(
                        text = stringResource(id = errorMessageRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = keyboardActions,
            modifier = Modifier
                .fillMaxWidth(),
            trailingIcon = {
                AnimatedVisibility(visible = value.isNotBlank()) {
                    IconButton(onClick = {
                        onValueChanged("")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            tint = MaterialTheme.colorScheme.secondary,
                            contentDescription = null
                        )
                    }
                }
            }
        )
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun TitledTextFieldPreview() {
    ReservationManagerTheme {
        TitledTextField(
            modifier = Modifier.padding(16.dp),
            title = "Name",
            isOptional = false,
            isError = false,
            errorMessageRes = R.string.name_is_required,
        )
    }
}