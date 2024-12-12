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
import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.ui.client.display.components.CustomerListWithTitle
import com.zaed.reservationmanager.ui.client.display.CustomerListViewModel
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme
import org.koin.androidx.compose.koinViewModel
import java.util.Date

@Composable
fun CustomerListScreen(
    viewModel: CustomerListViewModel = koinViewModel(),
    onShowNavDrawer: () -> Unit,
    onNavigateToAddCustomer: () -> Unit = {},
    onNavigateToEditCustomer:(Customer) -> Unit = {},
    onNavigateToCustomerDetails: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    CustomerListWithScreenContent(
        customers = state.customers,
        onNavigateToAddClient = onNavigateToAddCustomer,
        onShowNavDrawer = {
            onShowNavDrawer()
        },
        onViewCustomerDetails = { customerId ->
            onNavigateToCustomerDetails(customerId)
        },
        onEditCustomer = { customer ->
            onNavigateToEditCustomer(customer)
        },
        onDeleteCustomer = { customerId ->
            viewModel.deleteCustomer(customerId)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListWithScreenContent(
    customers: List<Customer>,
    onShowNavDrawer: () -> Unit = {},
    onNavigateToAddClient: () -> Unit = {},
    onViewCustomerDetails: (String) -> Unit = {},
    onDeleteCustomer: (String) -> Unit = {},
    onEditCustomer: (Customer) -> Unit = {}
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.customers),
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
            FloatingActionButton(onClick = { onNavigateToAddClient() }) {
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
            val countriesList = customers.map { it.residenceCountry }.distinct()
            LazyRow {
                items(countriesList) { country ->
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
            CustomerListWithTitle(
                customers =if (selected == "") customers else customers.filter { it.residenceCountry == selected },
                onViewCustomerDetailsClicked = onViewCustomerDetails,
                onDeleteCustomer = onDeleteCustomer,
                onEditCustomer = onEditCustomer
            )
        }
    }
}


val mockCustomers = listOf(
    Customer("1", "Alice", "USA", "Canada", "+123456789", "alice@example.com"),
    Customer("2", "Bob", "UK", "UK", "+987654321", "bob@example.com"),
    Customer(
        "3",
        "Charlie",
        "Canada",
        "USA",
        "+456789123",
        "charlie@example.com"
    )
)

@Composable
@Preview
fun CustomerListScreenPreview() {
    ReservationManagerTheme {
        CustomerListWithScreenContent(
            customers = mockCustomers,
            onShowNavDrawer = {}
        )
    }
}


