package com.zaed.reservationmanager.ui.company.details.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.ui.home.component.DetailRow
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme

@Composable
fun CompanyDetailsHeader(
    modifier: Modifier = Modifier,
    company: Company = Company(),
    onCopyPhone: (String) -> Unit = {},
    onMessagePhone: (String) -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = company.name,
            style = MaterialTheme.typography.titleLarge,
        )
        DetailRow(
            modifier = Modifier.padding(top = 8.dp),
            label = stringResource(R.string.country),
            value = company.country,
        )
        DetailRow(
            label = stringResource(R.string.phone_number_1),
            value = company.phoneNumber1,
            onClick = { onMessagePhone(company.phoneNumber1) },
            onLongClick = { onCopyPhone(company.phoneNumber1) },
        )
        DetailRow(
            label = stringResource(R.string.phone_number_2),
            value = company.phoneNumber2,
            onClick = { onMessagePhone(company.phoneNumber2) },
            onLongClick = { onCopyPhone(company.phoneNumber2) },
        )
        DetailRow(
            label = stringResource(R.string.email),
            value = company.email,
        )
        DetailRow(
            label = stringResource(R.string.fax_number),
            value = company.faxNumber,
            onLongClick = { onCopyPhone(company.faxNumber) },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    ReservationManagerTheme {
        val company = Company(
            name = "شركة الجوهرة للنقل السياحي",
            country = "Saudi Arabia",
            phoneNumber1 = "123456789",
            email = "email@test.com",
            faxNumber = "123456789",
        )
        CompanyDetailsHeader(
            modifier = Modifier.padding(16.dp),
            company = company,
            onCopyPhone = {},
            onMessagePhone = {},
        )
    }
}