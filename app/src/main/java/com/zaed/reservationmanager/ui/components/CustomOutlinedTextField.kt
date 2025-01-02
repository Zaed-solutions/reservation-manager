package com.zaed.reservationmanager.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults.Container
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextFieldDefaults.indicatorLine
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CustomOutlinedTextField(
    modifier: Modifier = Modifier,
    value: String = "",
    onValueChanged: (String) -> Unit = {},
    label: String = "",
    placeholder: String = "",
    isError: Boolean = false,
    supportingText: @Composable () -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    shape: Shape = RectangleShape,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    BasicTextField(
        value = value,
        readOnly = readOnly,
        enabled = enabled,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        onValueChange = onValueChanged,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        keyboardOptions = keyboardOptions,
        modifier = modifier.height(if(isError)50.dp else 35.dp),

        interactionSource = interactionSource,
        singleLine = singleLine,
    ) { innerTextField ->
        OutlinedTextFieldDefaults.DecorationBox(
            enabled = enabled,
            value = value,
            label = {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                )
            },
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            innerTextField = innerTextField,

            singleLine = true,
            visualTransformation = VisualTransformation.None,
            interactionSource = interactionSource,
            contentPadding = TextFieldDefaults.contentPaddingWithoutLabel(
                start = 8.dp,
                end = 8.dp,
                top = 4.dp,
                bottom = 4.dp
            ),
            isError = isError,
            supportingText = if(isError) supportingText else null,
            container = {
                Container(
                    enabled = enabled,
                    isError = isError,
                    interactionSource = interactionSource,
                    shape = shape,
                )
            }
        )
    }
}