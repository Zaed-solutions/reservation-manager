package com.zaed.reservationmanager.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme

@Composable
fun TitledTextField(
    modifier: Modifier = Modifier,
    title: String = "",
    initialValue: String = "",
    onValueChanged: (String) -> Unit = {},
    isOptional: Boolean = true,
    isError: Boolean = false,
    errorMessageRes: Int = 0,
    keyboardType: KeyboardType = KeyboardType.Unspecified
    ) {
    var value by remember { mutableStateOf(initialValue) }
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title + if (isOptional) "" else " *",
            style = MaterialTheme.typography.titleMedium,
        )
        OutlinedTextField(
            singleLine = true,
            value = value,
            onValueChange = {
                value = it
                onValueChanged(it)
            },
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
                keyboardType = keyboardType
            ),
            modifier = Modifier
                .fillMaxWidth(),
            trailingIcon = {
                AnimatedVisibility(visible = value.isNotBlank()) {
                    IconButton(onClick = { value = "" }) {
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