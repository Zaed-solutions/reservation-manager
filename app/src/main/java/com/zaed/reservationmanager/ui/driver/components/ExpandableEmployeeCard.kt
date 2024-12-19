package com.zaed.reservationmanager.ui.driver.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.ui.home.component.DetailRow
import com.zaed.reservationmanager.ui.util.formatEpochSecondsToDateTime

@Composable
fun ExpandableEmployeeCard(
    employee: Employee,
    isDriver: Boolean = false,
    onDeleteClicked: () -> Unit = {},
    onEditClicked: () -> Unit = {},
    onEmployeeDetailsClicked: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(4.dp),
        shape = RoundedCornerShape(8.dp),
        onClick = { expanded = !expanded },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, end = 16.dp, start = 16.dp, bottom = 0.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = employee.name.takeIf { it.length <= 15 }
                        ?: "${employee.name.take(15)}...",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = employee.phoneNumber1.takeIf { it.length <= 15 }
                        ?: "${employee.phoneNumber1.take(15)}...",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.End
                )

            }

            AnimatedVisibility(expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isDriver) {
                        DetailRow(
                            label = stringResource(R.string.nationality),
                            value = employee.nationality,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        DetailRow(
                            label = stringResource(R.string.position),
                            value = employee.position,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    DetailRow(
                        label = stringResource(R.string.phone_number_2),
                        value = employee.phoneNumber2,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    DetailRow(
                        label = stringResource(R.string.email),
                        value = employee.email,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    DetailRow(
                        label = stringResource(R.string.created_at),
                        value = employee.createdAtEpochSeconds.formatEpochSecondsToDateTime(),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(
                            contentPadding = PaddingValues(0.dp),
                            onClick = onEditClicked,
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.edit),
                                modifier = Modifier.wrapContentWidth()
                            )
                        }
                        TextButton(
                            contentPadding = PaddingValues(0.dp),
                            onClick = onDeleteClicked,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.delete),
                                modifier = Modifier.wrapContentWidth()
                            )
                        }
                    }
                }
            }
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
            )

        }
    }
}