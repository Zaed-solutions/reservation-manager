package com.zaed.reservationmanager.ui.employee.display

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.ui.client.create.CenterAlignedTopBar
import com.zaed.reservationmanager.ui.driver.components.EmployeeListWithTitle
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun EmployeeListScreen(
    viewModel: EmployeeListViewModel = koinViewModel(),
    onShowNavDrawer: () -> Unit,
    onNavigateToAddEmployee: () -> Unit,
    onNavigateToEditEmployee: (Employee) -> Unit,
    onNavigateToEmployeeDetails: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    EmployeeListWithScreenContent(
        employees = state.employees,
        onShowNavDrawer = onShowNavDrawer,
        onNavigateToAddEmployee = onNavigateToAddEmployee,
        onNavigateToEmployeeDetails = onNavigateToEmployeeDetails,
        onEditEmployee = {
            onNavigateToEditEmployee(it)
        },
        onDeleteEmployee = {
            viewModel.deleteEmployee(it)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeListWithScreenContent(
    employees: List<Employee>,
    onShowNavDrawer: () -> Unit = {},
    onNavigateToAddEmployee: () -> Unit = {},
    onNavigateToEmployeeDetails: () -> Unit = {},
    onEditEmployee: (Employee) -> Unit = {},
    onDeleteEmployee: (String) -> Unit = {}
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.employees),
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
            FloatingActionButton(onClick = { onNavigateToAddEmployee()} ) {
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
            val companiesList = employees.map { it.company }.distinct()
            LazyRow {
                items(companiesList) { country ->
                    FilterChip(
                        modifier = Modifier.padding(end = 8.dp),
                        onClick = {
                            if(selected == country) {
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
                employees = if (selected == "") employees else employees.filter { it.company == selected },
                onEmployeeDetailsClicked = { employeeId ->
                    onNavigateToEmployeeDetails()
                },
                onDeleteEmployee = onDeleteEmployee,
                onEditEmployee = onEditEmployee,
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
        EmployeeListWithScreenContent(
            employees = mockEmployees,
            onShowNavDrawer = {}
        )
    }
}


