import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.ui.components.TitledDropDownTextField
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(customers: List<Customer> = mockCustomers) {
    // Collect unique countries from the customer list
    val countries = customers.map { it.residenceCountry }.distinct().sorted()
    var selectedCountry by remember { mutableStateOf("") }
    var sortedCustomers by remember { mutableStateOf(customers.sortedByDescending { it.createdAt }) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customer List") },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(16.dp).padding(padding)) {
            // Dropdown Menu for Filtering
            var expandedCountry by remember { mutableStateOf(false) }
            var country by remember { mutableStateOf("") }
            ExposedDropdownMenuBox(
                expanded = expandedCountry,
                onExpandedChange = { expandedCountry = !expandedCountry }
            ) {
                TitledDropDownTextField(
                    title = "Filter by Country",
                    selectedValue = country,
                    onValueChanged = { index ->
                        country = countries[index]
//                        action(NewClientUiAction.UpdateCountry(countries[index]))
                    },
                    isOptional = false,
//                    isError = country.isBlank(),
                    options = countries
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sort Button
            Button(
                onClick = {
                    sortedCustomers = customers
                        .filter {
                            selectedCountry.isEmpty() || it.residenceCountry == selectedCountry
                        }
                        .sortedByDescending { it.createdAt }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Apply Filter & Sort")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display Table
            LazyColumn {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .border(1.dp, Color.Gray)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Id", modifier = Modifier.weight(0.3f))
                        Text("Name", modifier = Modifier.weight(1f))
                        Text("Nationality", modifier = Modifier.weight(1f))
                        Text("Residence Country", modifier = Modifier.weight(1f))
                    }
                }
                items(sortedCustomers) { customer ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .border(1.dp, Color.Gray)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(customer.id, modifier = Modifier.weight(0.3f))
                        Text(customer.name, modifier = Modifier.weight(1f))
                        Text(customer.nationality, modifier = Modifier.weight(1f))
                        Text(customer.residenceCountry, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

val mockCustomers = listOf(
    Customer("1", "Alice", "USA", "Canada", "+123456789", "alice@example.com", Date(2023, 11, 25)),
    Customer("2", "Bob", "UK", "UK", "+987654321", "bob@example.com", Date(2024, 12, 1)),
    Customer("3", "Charlie", "Canada", "USA", "+456789123", "charlie@example.com", Date(2024, 10, 15))
)

@Composable
@Preview
fun CustomerListScreenPreview() {
    ReservationManagerTheme {
        CustomerListScreen()
    }
}
