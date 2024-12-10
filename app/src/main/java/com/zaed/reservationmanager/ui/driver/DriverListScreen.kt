package com.zaed.reservationmanager.ui.driver

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.ui.client.create.CenterAlignedTopBar
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme
import org.koin.androidx.compose.koinViewModel
import java.util.Date

@Composable
fun DriverListScreen(
    viewModel: DriverListViewModel = koinViewModel(),
    navigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    DriverListWithScreenContent(
        drivers = state.drivers,
        onBackClicked = navigateBack
    )
}

@Composable
fun DriverListWithScreenContent(
    drivers: List<Employee>,
    onBackClicked: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            CenterAlignedTopBar(
                title = "Drivers List",
                onBackClicked = onBackClicked
            )
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
            DriverListWithTitle(
                employees =if (selected == "") drivers else drivers.filter { it.company == selected },
            )
        }
    }
}

@Composable
fun DriverListWithTitle(
    modifier: Modifier = Modifier,
    employees: List<Employee>,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(employees) { employee ->
            ExpandableEmployeeCard(
                employee = employee,
                onDeleteClicked = {},
                onEditClicked = {}
            )
        }
    }
}

@Composable
fun ExpandableEmployeeCard(
    employee: Employee,
    onDeleteClicked: () -> Unit = {},
    onEditClicked: () -> Unit = {}

) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(4.dp),
        shape = RoundedCornerShape(8.dp),
        onClick = { expanded = !expanded },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, end = 16.dp, start = 16.dp, bottom = 0.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = employee.name.takeIf { it.length <= 15 }
                        ?: "${employee.name.take(15)}...",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = employee.phoneNumber1.takeIf { it.length <= 15 }
                        ?: "${employee.phoneNumber1.take(15)}...",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )

            }

            AnimatedVisibility(expanded) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Nationality: ${employee.nationality}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Residence Country: ${employee.phoneNumber2}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Email: ${employee.email}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Created At: ${employee.createdAtEpochSeconds}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(
                            contentPadding = PaddingValues(0.dp),
                            onClick = onDeleteClicked,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Delete",
                                modifier = Modifier.wrapContentWidth()
                            )
                        }


                        TextButton(
                            contentPadding = PaddingValues(0.dp),
                            onClick = onEditClicked,
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Edit",
                                modifier = Modifier.wrapContentWidth()
                            )
                        }
                    }
                }
            }
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
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
            onBackClicked = {}
        )
    }
}


