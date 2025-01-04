package com.zaed.reservationmanager.ui.employee.display

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.ui.driver.components.EmployeeListWithTitle
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme
import com.zaed.reservationmanager.ui.util.PhoneUtil
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
    val context = LocalContext.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
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
        },
        onMessagePhone = {
            PhoneUtil.sendWhatsappMessage(
                context = context,
                phoneNumber = it,
                message = "",
                onFailure = {
                    Toast.makeText(context, context.getString(R.string.whatsapp_is_not_installed), Toast.LENGTH_SHORT).show()
                }
            )
        },
        onCopyPhone = {
            clipboardManager.setText(AnnotatedString(it))
            Toast.makeText(context, context.getString(R.string.number_copied_to_clipboard), Toast.LENGTH_SHORT).show()
        },
        onSaveToContacts = { employee->
            PhoneUtil.saveToContacts(context,employee.name,employee.phoneNumber1,employee.phoneNumber2,employee.email,employee.company )
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
    onDeleteEmployee: (String) -> Unit = {},
    onMessagePhone: (String) -> Unit = {},
    onCopyPhone: (String) -> Unit = {},
    onSaveToContacts: (Employee) -> Unit = {}
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
            FloatingActionButton(onClick = { onNavigateToAddEmployee() }) {
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
            var searchQuery by remember { mutableStateOf("") }
            val filteredEmployee = employees.filter { employee ->
                listOf(
                    employee.name,
                    employee.phoneNumber1,
                    employee.phoneNumber2,
                ).any { value ->
                    value.contains(searchQuery, ignoreCase = true)
                }
            }.sortedBy { customer -> customer.name }
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    val data =if(it.matches(Regex("[+\\d\\s]+"))) it.replace(" ","") else it
//                    onAction(HomeUiAction.UpdateSearchQuery(data))
                    searchQuery = data

                },
                placeholder = { Text(stringResource(R.string.smart_search)) },
                modifier = Modifier
                    .fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = {
//                                onAction(HomeUiAction.UpdateSearchQuery(""))
                                searchQuery = ""
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = null
                            )
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.large
            )
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
                employees = if (selected == "") filteredEmployee else filteredEmployee.filter { it.company == selected },
                onEmployeeDetailsClicked = { employeeId ->
                    onNavigateToEmployeeDetails()
                },
                onDeleteEmployee = onDeleteEmployee,
                onEditEmployee = onEditEmployee,
                onMessagePhone = onMessagePhone,
                onCopyPhone = onCopyPhone,
                onSaveToContacts = onSaveToContacts
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


