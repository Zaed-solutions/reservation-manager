package com.zaed.reservationmanager.ui.home.component

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
    onDeleteCustomer: (String) -> Unit = {},
    onEditCustomer: (Customer) -> Unit = {},
    onMessagePhoneNumber: (String) -> Unit = {},
    onCopyPhoneNumber: (String) -> Unit = {},
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
                onDeleteClicked = {
                    onDeleteCustomer(customer.id)
                },
                onEditClicked = {
                    onEditCustomer(customer)
                },
                onMessagePhoneNumber = {
                    onMessagePhoneNumber(customer.phoneNumber1)
                },
                onCopyPhoneNumber = {
                    onCopyPhoneNumber(customer.phoneNumber1)
                }
            )
        }
    }
}