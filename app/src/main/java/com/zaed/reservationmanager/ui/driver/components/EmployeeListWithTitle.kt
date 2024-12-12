package com.zaed.reservationmanager.ui.driver.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.data.model.Employee

@Composable
fun EmployeeListWithTitle(
    modifier: Modifier = Modifier,
    employees: List<Employee>,
    isDriver: Boolean = false,
    onEmployeeDetailsClicked: (String) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(employees) { employee ->
            ExpandableEmployeeCard(
                employee = employee,
                isDriver = isDriver,
                onDeleteClicked = {},
                onEditClicked = {},
                onEmployeeDetailsClicked = {
                    onEmployeeDetailsClicked(employee.id)
                }
            )
        }
    }
}