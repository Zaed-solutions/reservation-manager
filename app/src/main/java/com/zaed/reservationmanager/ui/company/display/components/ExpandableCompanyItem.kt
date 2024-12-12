package com.zaed.reservationmanager.ui.company.display.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.ui.reservation.details.components.DetailRow
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme

@Composable
fun ExpandableCompanyItem(
    modifier: Modifier = Modifier,
    company: Company = Company(),
    onCompanyDetailsClicked: () -> Unit = {},
    onDeleteCompany: () -> Unit = {},
    onEditCompany: () -> Unit = {}
) {
    var isExpanded by remember {
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
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = company.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = company.country,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top=8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.Start,
                ) {
                    DetailRow(
                        label = stringResource(R.string.phone_number),
                        value = company.phoneNumber
                    )
                    DetailRow(
                        label = stringResource(R.string.fax_number),
                        value = company.faxNumber
                    )
                    DetailRow(
                        label = stringResource(R.string.email),
                        value = company.email
                    )
                    Text(
                        text = stringResource(R.string.view_details),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            onCompanyDetailsClicked()
                        }
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Button(
                            modifier = Modifier.heightIn(min = 0.dp, max = 28.dp),
                            onClick = { onEditCompany() },
                            colors = ButtonDefaults.buttonColors(
                                contentColor = MaterialTheme.colorScheme.primary,
                                containerColor = Color.Transparent
                            ),
                            contentPadding = PaddingValues(top = 0.dp, bottom = 0.dp, start = 0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                            )
                            Text(
                                text = stringResource(R.string.edit),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                        Button(
                            modifier = Modifier.heightIn(min = 0.dp, max = 28.dp),
                            onClick = { onDeleteCompany() },
                            colors = ButtonDefaults.buttonColors(
                                contentColor = MaterialTheme.colorScheme.error,
                                containerColor = Color.Transparent
                            ),
                            contentPadding = PaddingValues(vertical = 0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                            )
                            Text(
                                text = stringResource(R.string.delete),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
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

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun ExpandableCompanyItemPreview() {
    ReservationManagerTheme {
        ExpandableCompanyItem(
            modifier = Modifier.padding(16.dp),
            company = Company(
                name = "Company Name",
                country = "Country",
                phoneNumber = "123456789",
                faxNumber = "123456789",
                email = "company-name@test.com"
            ),
        )
    }
}