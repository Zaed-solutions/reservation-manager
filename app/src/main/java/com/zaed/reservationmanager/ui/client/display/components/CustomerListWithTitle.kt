package com.zaed.reservationmanager.ui.client.display.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.data.model.Customer

@Composable
fun CustomerListWithTitle(
    modifier: Modifier = Modifier,
    customers: List<Customer>,
    onViewCustomerDetailsClicked: (String) -> Unit = {},
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(customers) { customer ->
            ExpandableCustomerCard(
                customer = customer,
                onViewDetailsClicked = {
                    onViewCustomerDetailsClicked(customer.id)
                },
                onDeleteClicked = {},
                onEditClicked = {}
            )
        }
    }
}