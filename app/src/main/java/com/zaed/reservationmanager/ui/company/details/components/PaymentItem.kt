package com.zaed.reservationmanager.ui.company.details.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.CompanyPayment
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme
import com.zaed.reservationmanager.ui.util.formatEpochSecondsToDate
import kotlinx.datetime.Clock

@Composable
fun PaymentItem(
    modifier: Modifier = Modifier,
    payment: CompanyPayment,
    onEditPayment: () -> Unit = {},
    onDeletePayment: () -> Unit = {}
) {
    var isOptionMenuVisible by remember {
        mutableStateOf(false)
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = payment.createdAtEpochSeconds.formatEpochSecondsToDate(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
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
                                onEditPayment()
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
                                onDeletePayment()
                                isOptionMenuVisible = false
                            },
                            text = {
                                Text(
                                    text = stringResource(R.string.delete),
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = payment.description,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(R.string.sar, payment.amount.toString()),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    ReservationManagerTheme {
        PaymentItem(
            payment = CompanyPayment(
                id = "1",
                companyId = "1",
                amount = 100.0,
                description = "Payment description",
                createdAtEpochSeconds = Clock.System.now().epochSeconds
            )
        )
    }
}