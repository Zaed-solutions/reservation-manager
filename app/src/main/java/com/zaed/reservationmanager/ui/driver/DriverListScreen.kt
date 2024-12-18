package com.zaed.reservationmanager.ui.driver

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.ui.driver.components.EmployeeListWithTitle
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun DriverListScreen(
    viewModel: DriverListViewModel = koinViewModel(),
    onShowNavDrawer: () -> Unit,
    onNavigateToAddDriver: () -> Unit,
    onNavigateToEditDriver: (Employee) -> Unit,
    onNavigateToEmployeeDetails: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    DriverListWithScreenContent(
        drivers = state.drivers,
        onShowNavDrawer = onShowNavDrawer,
        onNavigateToAddDriver = onNavigateToAddDriver,
        onNavigateToEmployeeDetails = onNavigateToEmployeeDetails,
        onEditEmployee = {
            onNavigateToEditDriver(it)
        },
        onDeleteEmployee = {
            viewModel.deleteEmployee(it)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverListWithScreenContent(
    drivers: List<Employee>,
    onShowNavDrawer: () -> Unit = {},
    onNavigateToAddDriver: () -> Unit = {},
    onNavigateToEmployeeDetails: (String) -> Unit = {},
    onEditEmployee: (Employee) -> Unit = {},
    onDeleteEmployee: (String) -> Unit = {}
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.drivers),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onShowNavDrawer() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = null
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToAddDriver() }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            var selected by remember { mutableStateOf("") }
            val companiesList = drivers.map { it.company }.distinct()
            LazyRow {
                items(companiesList) { country ->
                    FilterChip(
                        modifier = Modifier.padding(end = 8.dp),
                        onClick = {
                            if (selected == country) {
                                selected = ""
                            } else {
                                selected = country
                            }
                        },
                        label = {
                            Text(country)
                        },
                        selected = selected == country,
                        leadingIcon = if (selected == country) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Done,
                                    contentDescription = "Done icon",
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        } else {
                            null
                        },
                    )
                }
            }
            EmployeeListWithTitle(
                employees = if (selected == "") drivers else drivers.filter { it.company == selected },
                isDriver = true,
                onEmployeeDetailsClicked = { employeeId ->
                    onNavigateToEmployeeDetails(employeeId)
                },
                onDeleteEmployee = onDeleteEmployee,
                onEditEmployee = onEditEmployee
            )
        }
    }
}


val mockEmployees = listOf(
    Employee(
        id = "1",
        name = "John Doe",
        nationality = "USA",
        company = "ABC Company",
        phoneNumber1 = "123-456-7890",
        phoneNumber2 = "9787559555",
        email = "william.henry.moody@my-own-personal-domain.com",
    ),
    Employee(
        id = "1",
        name = "John Doe",
        nationality = "USA",
        company = "AB Company",
        phoneNumber1 = "123-456-7890",
        phoneNumber2 = "9787559555",
        email = "william.henry.moody@my-own-personal-domain.com",
    ),


    )


@Composable
@Preview
fun CustomerListScreenPreview() {
    ReservationManagerTheme {
        DriverListWithScreenContent(
            drivers = mockEmployees,
            onShowNavDrawer = {}
        )
    }
}


