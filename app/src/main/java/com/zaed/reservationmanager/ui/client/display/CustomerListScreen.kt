import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.ui.client.display.CustomerListViewModel
import com.zaed.reservationmanager.ui.client.display.components.CustomerListWithTitle
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme
import com.zaed.reservationmanager.ui.util.PdfUtil.exportCustomersToPdf
import com.zaed.reservationmanager.ui.util.SheetUtil.exportCustomersToExcel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun CustomerListScreen(
    viewModel: CustomerListViewModel = koinViewModel(),
    onShowNavDrawer: () -> Unit,
    onNavigateToAddCustomer: () -> Unit = {},
    onNavigateToEditCustomer: (Customer) -> Unit = {},
    onNavigateToCustomerDetails: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    CustomerListWithScreenContent(
        displayedCustomers = state.displayedCustomers,
        countriesList = state.countries,
        selectedCountry = state.selectedCountry,
        onFilterCountries = { country ->
            viewModel.filterByCountry(country)
        },
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
        },
        onExportCustomersAsCSV = {
            val file = state.displayedCustomers.exportCustomersToExcel(
                context = context,
                headers = listOf(
                    context.getString(R.string.name),
                    context.getString(R.string.nationality),
                    context.getString(R.string.residence_country),
                    context.getString(R.string.phone_number),
                    context.getString(R.string.email),
                )
            )
            scope.launch {
                if (file != null) {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.pdf_saved_at, file.path),
                        actionLabel = context.getString(R.string.open)
                    ).let { result ->
                        if (result == SnackbarResult.ActionPerformed) {
                            try {
                                val openFileIntent = Intent(Intent.ACTION_VIEW).apply {
                                    val fileUri: Uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )
                                    setDataAndType(fileUri, "text/csv")
                                    flags =
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                if (openFileIntent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(openFileIntent)
                                } else {
                                    snackbarHostState.showSnackbar(context.getString(R.string.no_csv_viewer_found))
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                } else {
                    snackbarHostState.showSnackbar(context.getString(R.string.error_exporting_csv))
                }
            }
        },
        hostState = snackbarHostState,
        onExportCustomersAsPDF = {
            val file = state.displayedCustomers.exportCustomersToPdf(
                context = context,
                headers = listOf(
                    context.getString(R.string.name),
                    context.getString(R.string.nationality),
                    context.getString(R.string.residence_country),
                    context.getString(R.string.phone_number),
                    context.getString(R.string.email),
                ),
                title = if (state.selectedCountry.isBlank()) {
                    context.getString(R.string.customers_list)
                } else {
                    context.getString(R.string.customers_list_for_country, state.selectedCountry)
                }
            )
            scope.launch {
                if (file != null) {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.pdf_saved_at, file.path),
                        actionLabel = context.getString(R.string.open)
                    ).let { result ->
                        if (result == SnackbarResult.ActionPerformed) {
                            try {
                                val openFileIntent = Intent(Intent.ACTION_VIEW).apply {
                                    val fileUri: Uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )
                                    setDataAndType(fileUri, "application/pdf")
                                    flags =
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                if (openFileIntent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(openFileIntent)
                                } else {
                                    snackbarHostState.showSnackbar(context.getString(R.string.no_pdf_viewer_found))
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                } else {
                    snackbarHostState.showSnackbar(context.getString(R.string.error_exporting_pdf))
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListWithScreenContent(
    displayedCustomers: List<Customer> = emptyList(),
    countriesList: List<String> = emptyList(),
    hostState: SnackbarHostState,
    selectedCountry: String = "",
    onFilterCountries: (String) -> Unit = {},
    onShowNavDrawer: () -> Unit = {},
    onNavigateToAddClient: () -> Unit = {},
    onViewCustomerDetails: (String) -> Unit = {},
    onDeleteCustomer: (String) -> Unit = {},
    onEditCustomer: (Customer) -> Unit = {},
    onExportCustomersAsCSV: () -> Unit = {},
    onExportCustomersAsPDF: () -> Unit = {},
) {
    var isOptionsMenuVisible by remember {
        mutableStateOf(false)
    }
    Scaffold(
        snackbarHost = { SnackbarHost(hostState) },
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
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .wrapContentSize(Alignment.TopEnd)
                    ) {
                        IconButton(
                            onClick = { isOptionsMenuVisible = !isOptionsMenuVisible },
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = null,
                            )
                        }
                        DropdownMenu(
                            expanded = isOptionsMenuVisible,
                            onDismissRequest = { isOptionsMenuVisible = false }
                        ) {
                            DropdownMenuItem(
                                onClick = {
                                    onExportCustomersAsCSV()
                                    isOptionsMenuVisible = false
                                },
                                text = {
                                    Text(
                                        text = stringResource(R.string.export_as_csv),
                                    )
                                },
                            )
//                            DropdownMenuItem(
//                                onClick = {
//                                    onExportCustomersAsPDF()
//                                    isOptionsMenuVisible = false
//                                },
//                                text = {
//                                    Text(
//                                        text = stringResource(R.string.export_as_pdf),
//                                    )
//                                },
//                            )
                        }
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
            LazyRow {
                items(countriesList) { country ->
                    FilterChip(
                        modifier = Modifier.padding(end = 8.dp),
                        onClick = {
                            onFilterCountries(if (selectedCountry == country) "" else country)
                        },
                        label = {
                            Text(country)
                        },
                        selected = selectedCountry == country,
                        leadingIcon = if (selectedCountry == country) {
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
                customers = displayedCustomers,
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
            displayedCustomers = mockCustomers,
            onShowNavDrawer = {},
            hostState = SnackbarHostState()
        )
    }
}


