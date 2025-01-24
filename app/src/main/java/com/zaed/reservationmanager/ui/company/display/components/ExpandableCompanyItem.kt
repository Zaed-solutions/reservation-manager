package com.zaed.reservationmanager.ui.company.display.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.zaed.reservationmanager.data.model.CompanyType
import com.zaed.reservationmanager.data.model.CompanyWithBalance
import com.zaed.reservationmanager.ui.home.component.DetailRow
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpandableCompanyItem(
    modifier: Modifier = Modifier,
    companyWithBalance: CompanyWithBalance = CompanyWithBalance(),
    onCompanyDetailsClicked: () -> Unit = {},
    onDeleteCompany: () -> Unit = {},
    onEditCompany: () -> Unit = {},
    onCopyPhone: (String) -> Unit = {},
    onMessagePhone: (String) -> Unit = {},
    onSaveToContacts: () -> Unit = {},
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
    val company = companyWithBalance.company
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = company.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.combinedClickable(
                        onClick = {
                            onMessagePhone(company.phoneNumber1)
                        },
                        onLongClick = {
                            onCopyPhone(company.phoneNumber1)
                        }
                    ),
                )
                if (company.type == CompanyType.TOURISM) {
                    Text(
                        text = company.country,
                        style = MaterialTheme.typography.titleMedium,
                    )
                } else {
                    Text(
                        text = company.city,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Text(
                    text = stringResource(
                        R.string.sar,
                        NumberFormat.getInstance(Locale.getDefault()).format(companyWithBalance.balance)),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.Start,
                ) {
                    DetailRow(
                        label = stringResource(R.string.phone_number_1),
                        value = company.phoneNumber1,
                        style = MaterialTheme.typography.bodyMedium,
                        onClick = { onMessagePhone(company.phoneNumber1) },
                        onLongClick = { onCopyPhone(company.phoneNumber1) },
                    )
                    DetailRow(
                        label = stringResource(R.string.phone_number_2),
                        value = company.phoneNumber2,
                        style = MaterialTheme.typography.bodyMedium,
                        onClick = { onMessagePhone(company.phoneNumber2) },
                        onLongClick = { onCopyPhone(company.phoneNumber2) },
                    )
                    DetailRow(
                        label = stringResource(R.string.email),
                        value = company.email,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    DetailRow(
                        label = stringResource(R.string.fax_number),
                        value = company.faxNumber,
                        style = MaterialTheme.typography.bodyMedium
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
                        TextButton(
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
                        TextButton(
                            modifier = Modifier.heightIn(min = 0.dp, max = 28.dp),
                            onClick = { onSaveToContacts() },
                            colors = ButtonDefaults.buttonColors(
                                contentColor = MaterialTheme.colorScheme.primary,
                                containerColor = Color.Transparent
                            ),
                            contentPadding = PaddingValues(top = 0.dp, bottom = 0.dp, start = 0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Contacts,
                                contentDescription = null,
                            )
                            Text(
                                text = stringResource(R.string.save),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                        TextButton(
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

@Preview(showSystemUi = true, showBackground = true, locale = "ar")
@Composable
private fun ExpandableCompanyItemPreview() {
    ReservationManagerTheme {
        ExpandableCompanyItem(
            modifier = Modifier.padding(16.dp),
            companyWithBalance = CompanyWithBalance(
                Company(
                    name = "Company Name",
                    type = CompanyType.TRAVEL,
                    country = "Country",
                    city = "p",
                    phoneNumber1 = "123456789",
                    faxNumber = "123456789",
                    email = "company-name@test.com"
                ), 1250
            )
        )
    }
}