package com.zaed.reservationmanager.ui.client.details.components

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
import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.ui.home.component.DetailRow
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme

@Composable
fun CustomerDetailsHeader(
    modifier: Modifier = Modifier,
    customer: Customer = Customer(),
    onCopyPhone: (String) -> Unit = {},
    onMessagePhone: (String) -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = customer.name,
            style = MaterialTheme.typography.titleLarge,
        )
        DetailRow(
            label = stringResource(R.string.nationality),
            value = customer.nationality,
        )
        DetailRow(
            label = stringResource(R.string.residence_country),
            value = customer.residenceCountry,
        )
        DetailRow(
            label = stringResource(R.string.city),
            value = customer.city,
        )
        DetailRow(
            label = stringResource(R.string.job),
            value = customer.job,
        )
        DetailRow(
            label = stringResource(R.string.phone_number_1),
            value = customer.phoneNumber1,
            onClick = { onMessagePhone(customer.phoneNumber1) },
            onLongClick = { onCopyPhone(customer.phoneNumber1) },
        )
        DetailRow(
            label = stringResource(R.string.phone_number_2),
            value = customer.phoneNumber2,
            onClick = { onMessagePhone(customer.phoneNumber2) },
            onLongClick = { onCopyPhone(customer.phoneNumber2) },
        )
        DetailRow(
            label = stringResource(R.string.email),
            value = customer.email,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    ReservationManagerTheme {
        CustomerDetailsHeader(
            modifier = Modifier.padding(16.dp),
            customer = Customer(
                name = "John Doe",
                nationality = "American",
                residenceCountry = "United States",
                phoneNumber1 = "+1234567890",
                email = "test@test.com"
            )
        )
    }
}