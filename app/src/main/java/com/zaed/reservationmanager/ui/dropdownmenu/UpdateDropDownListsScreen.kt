import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.ui.dropdownmenu.UpdateDropDownListsViewModel
import com.zaed.reservationmanager.ui.util.Constants.CAR_TYPES_KEY
import com.zaed.reservationmanager.ui.util.Constants.COUNTRIES_KEY
import com.zaed.reservationmanager.ui.util.Constants.RESERVATION_TYPES_KEY
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

// DataStore Helper Functions


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateDropDownListsScreen(
    onNavDrawerClicked: () -> Unit,
    viewModel: UpdateDropDownListsViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()
    // Initialize menus
    LaunchedEffect(Unit) {
        scope.launch {
            viewModel.initializeMenus()
        }
    }

    val reservationTypesFlow = viewModel.getMenus(RESERVATION_TYPES_KEY)
        .collectAsState(initial = emptySet())
    val carTypesFlow = viewModel.getMenus(CAR_TYPES_KEY)
        .collectAsState(initial = emptySet())
    val countriesFlow = viewModel.getMenus(COUNTRIES_KEY)
        .collectAsState(initial = emptySet())

    var selectedMenuKey by remember { mutableStateOf<Preferences.Key<Set<String>>?>(null) }
    var isEditing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.manage_menus)) },
                navigationIcon = {
                    IconButton(onClick = onNavDrawerClicked) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(stringResource(R.string.select_a_menu_to_manage), style = MaterialTheme.typography.titleMedium)
            TextButton({
                if (selectedMenuKey == RESERVATION_TYPES_KEY) {
                    selectedMenuKey = null
                } else {
                    selectedMenuKey = RESERVATION_TYPES_KEY
                }
            }) {
                Text(stringResource(R.string.reservation_types), style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    isEditing = true
                    selectedMenuKey = RESERVATION_TYPES_KEY
                }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Delete")
                }
            }
            AnimatedVisibility(selectedMenuKey == RESERVATION_TYPES_KEY) {
                MenuList(
                    reservationTypesFlow.value,
                ){
                    viewModel.deleteMenu(RESERVATION_TYPES_KEY, it)
                }
            }
            TextButton({
                if (selectedMenuKey == CAR_TYPES_KEY) {
                    selectedMenuKey = null
                } else {
                    selectedMenuKey = CAR_TYPES_KEY
                }
            }) {
                Text(stringResource(R.string.car_types), style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    isEditing = true
                    selectedMenuKey = CAR_TYPES_KEY
                }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Delete")
                }
            }
            AnimatedVisibility(selectedMenuKey == CAR_TYPES_KEY) {
                MenuList(carTypesFlow.value){
                    viewModel.deleteMenu(CAR_TYPES_KEY, it)
                }
            }
            TextButton({
                if (selectedMenuKey == COUNTRIES_KEY) {
                    selectedMenuKey = null
                } else {
                    selectedMenuKey = COUNTRIES_KEY
                }
            }) {
                Text(stringResource(R.string.countries), style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    isEditing = true
                    selectedMenuKey = COUNTRIES_KEY
                }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Delete")
                }
            }
            AnimatedVisibility(selectedMenuKey == COUNTRIES_KEY) {
                MenuList(countriesFlow.value) {
                    viewModel.deleteMenu(COUNTRIES_KEY, it)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

        }
        if (isEditing) {
            var text by remember { mutableStateOf("") }
            BasicAlertDialog(
                onDismissRequest = { isEditing = false },
                content = {
                    Surface {
                        Column(Modifier.padding(16.dp)) {
                            Text(stringResource(R.string.enter_a_new_item))
                            OutlinedTextField(
                                value = text,
                                label = { Text(stringResource(R.string.new_item)) },
                                onValueChange = { text = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = {
                                    if (text.isNotEmpty())
                                        scope.launch {
                                            viewModel.saveMenus(
                                                selectedMenuKey!!,
                                                if (selectedMenuKey == RESERVATION_TYPES_KEY) {
                                                    reservationTypesFlow.value.toMutableSet()
                                                        .apply {
                                                            add(text)
                                                        }
                                                } else if (selectedMenuKey == CAR_TYPES_KEY) {
                                                    carTypesFlow.value.toMutableSet().apply {
                                                        add(text)
                                                    }
                                                } else {
                                                    countriesFlow.value.toMutableSet().apply {
                                                        add(text)
                                                    }
                                                }
                                            )
                                        }
                                    isEditing = false
                                }) {
                                    Text(stringResource(R.string.confirm))
                                }
                                TextButton(onClick = {
                                    isEditing = false
                                }) {
                                    Text(stringResource(R.string.cancel))
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun MenuList(
    menuItems: Set<String>,
    deleteItem: (String) -> Unit
) {
    val menuList = menuItems.toList()
    LazyColumn {
        items(menuList) { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(item, modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    deleteItem(item)
                }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}


