package com.zaed.reservationmanager.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitledDropDownTextField(
    modifier: Modifier = Modifier,
    title: String = "",
    selectedValue: String,
    options: List<String>,
    onValueChanged: (Int) -> Unit = {},
    isOptional: Boolean = true,
    isError: Boolean = false,
    errorMessageRes: Int = 0,
) {
    var expanded by remember { mutableStateOf(false) }
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
        ExposedDropdownMenuBox(
            modifier = Modifier.fillMaxWidth(),
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                readOnly = true,
                value = selectedValue,
                onValueChange = {},
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
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
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEachIndexed { index, option: String ->
                    DropdownMenuItem(
                        text = { Text(text = option) },
                        onClick = {
                            expanded = false
                            onValueChanged(index)
                        }
                    )
                }
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun TitledDropDownTextFieldPreview() {
    ReservationManagerTheme {
        TitledDropDownTextField(
            modifier = Modifier.padding(16.dp),
            title = "Fruit",
            selectedValue = "Apple",
            options = listOf("Apple", "Banana", "Cherry"),
        )
    }
}