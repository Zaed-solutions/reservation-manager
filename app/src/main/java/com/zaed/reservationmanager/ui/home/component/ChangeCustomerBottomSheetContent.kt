package com.zaed.reservationmanager.ui.home.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.ui.client.create.ClientUIError
import com.zaed.reservationmanager.ui.client.create.CreateCustomerUiAction
import com.zaed.reservationmanager.ui.components.TitledDropDownTextField
import com.zaed.reservationmanager.ui.components.TitledTextField2
import com.zaed.reservationmanager.ui.util.InputValidator
import kotlinx.coroutines.flow.update

@Composable
fun ChangeCustomerBottomSheetContent(
    modifier: Modifier = Modifier,
    customers: List<Customer>,
    countries: List<String>,
    onCustomerSelected: (Customer) -> Unit,
    onAddNewCustomer: (Customer) -> Unit,
) {
    var isCreatingNewCustomer: Boolean? by remember {
        mutableStateOf(null)
    }
    var isLoading by remember { mutableStateOf(false) }
    BackHandler { isCreatingNewCustomer = null }
    AnimatedContent(isCreatingNewCustomer) { state ->
        Column(
            modifier = modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp))
                    }
                }
                state == true -> {
                    AddNewCustomerSection (
                        countries = countries,
                        customers = customers,
                        onAddNewCustomer = {
                            isLoading = true
                            onAddNewCustomer(it)
                        }
                    )
                }

                state == false -> {
                    SelectExistingCustomerSection(
                        customers = customers,
                        onCustomerSelected = {
                            isLoading = true
                            onCustomerSelected(it)
                        }
                    )
                }

                else -> {
                    //select options
                    SelectCustomerOptionSection(
                        onOptionSelected = {
                            isCreatingNewCustomer = it
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectExistingCustomerSection(
    modifier: Modifier = Modifier,
    customers: List<Customer> = emptyList(),
    onCustomerSelected: (Customer) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredCustomers = customers.filter { customer ->
        listOf(
            customer.name,
            customer.phoneNumber1,
            customer.phoneNumber2
        ).any { value ->
            value.contains(searchQuery, ignoreCase = true)
        }
    }.sortedBy { customer -> customer.name }
    Column (
        modifier = modifier.fillMaxWidth().height(600.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                val data = if (it.matches(Regex("[+\\d\\s]+"))) it.replace(" ", "") else it
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
        LazyColumn (
            modifier = Modifier.fillMaxWidth()
                .heightIn(min = 0.dp, max = 500.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(filteredCustomers){ customer ->
              Surface (
                  modifier = Modifier.fillMaxWidth().animateItem(),
                  onClick = {
                      onCustomerSelected(customer)
                  },
                  contentColor = MaterialTheme.colorScheme.onSurface,
                  color = Color.Transparent
              ){
                  Row(
                      modifier = Modifier.fillMaxWidth().padding(16.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                      Text(
                          text = customer.name.takeIf { it.length <= 15 }
                              ?: "${customer.name.take(15)}...",
                          style = MaterialTheme.typography.titleMedium,
                          modifier = Modifier.weight(1f)
                      )
                      Text(
                          text = customer.phoneNumber1.takeIf { it.length <= 15 }
                              ?: "${customer.phoneNumber1.take(15)}...",
                          style = MaterialTheme.typography.bodyLarge,
                          textAlign = TextAlign.End
                      )
                  }
              }
          }
        }
    }
}

@Composable
private fun AddNewCustomerSection(
    modifier: Modifier = Modifier,
    customers: List<Customer> = emptyList(),
    countries: List<String> = emptyList(),
    onAddNewCustomer: (Customer) -> Unit
) {
    var customer by remember {
        mutableStateOf(Customer())
    }
    var error by remember {
        mutableStateOf<ClientUIError>(ClientUIError.NONE)
    }
    Column (
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
    ){
        TitledTextField2(
            title = stringResource(R.string.client_name),
            value = customer.name,
            onValueChanged = { newText ->
                customer = customer.copy(name = newText)
            },
            isOptional = false,
            isError = error in listOf(
                ClientUIError.NAME_IS_REQUIRED
            ),
            errorMessageRes = error.messageRes,
            keyboardType = KeyboardType.Text
        )
        TitledDropDownTextField(
            title = stringResource(R.string.nationality),
            selectedValue = customer.nationality,
            onValueChanged = { index ->
                customer = customer.copy(nationality = countries.getOrNull(index) ?: "")
            },
            isOptional = true,
            isError = false,
            errorMessageRes = R.string.nationality_is_required,
            options = countries
        )
        TitledDropDownTextField(
            title = stringResource(R.string.country_of_residence),
            selectedValue = customer.residenceCountry,
            onValueChanged = { index ->
                customer = customer.copy(residenceCountry = countries.getOrNull(index) ?: "")
            },
            isOptional = false,
            errorMessageRes = R.string.country_is_required,
            options = countries
        )
        TitledTextField2(
            title = stringResource(R.string.city),
            value = customer.city,
            onValueChanged = { newText ->
                customer = customer.copy(city = newText)
            },
            isOptional = true,
        )
        TitledTextField2(
            title = stringResource(R.string.job),
            value = customer.job,
            onValueChanged = { newText ->
                customer = customer.copy(job = newText)
            },
            isOptional = true,
        )
        TitledTextField2(
            title = stringResource(R.string.phone_number_1),
            value = customer.phoneNumber1,
            onValueChanged = { newText ->
                customer = customer.copy(phoneNumber1 = newText)
            },
            isOptional = false,
            isError = error in listOf(
                ClientUIError.PHONE_NUMBER_IS_REQUIRED,
                ClientUIError.PHONE_NUMBER_1_IS_IN_USE,
                ClientUIError.PHONE_NUMBER_1_IS_INVALID
            ),
            errorMessageRes = error.messageRes,
            keyboardType = KeyboardType.Phone
        )
        TitledTextField2(
            title = stringResource(R.string.phone_number_2),
            value = customer.phoneNumber2,
            onValueChanged = { newText ->
                customer = customer.copy(phoneNumber2 = newText)
            },
            isError = (error in listOf(
                ClientUIError.PHONE_NUMBER_2_IS_INVALID,
                ClientUIError.PHONE_NUMBER_2_IS_IN_USE
            )) && customer.phoneNumber2.isNotBlank(),
            errorMessageRes = error.messageRes,
            isOptional = true,
            keyboardType = KeyboardType.Phone
        )
        TitledTextField2(
            title = stringResource(R.string.email),
            value = customer.email,
            onValueChanged = { newText ->
                customer = customer.copy(email = newText)
            },
            isOptional = true,
            isError = error in listOf(ClientUIError.EMAIL_IS_INVALID),
            errorMessageRes = error.messageRes,
            keyboardType = KeyboardType.Email
        )
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            onClick = {
                error = customer.validate(customers)
                if (error == ClientUIError.NONE) {
                    onAddNewCustomer(customer)
                }
            },
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = stringResource(R.string.add_customer),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

private fun Customer.validate(customers: List<Customer>): ClientUIError{
    return when{
        this.name.isBlank() -> ClientUIError.NAME_IS_REQUIRED
        this.phoneNumber1.isBlank() -> ClientUIError.PHONE_NUMBER_IS_REQUIRED
        !InputValidator.isPhoneNumberValid(this.phoneNumber1) -> ClientUIError.PHONE_NUMBER_1_IS_INVALID
        this.phoneNumber2.isNotBlank() && !InputValidator.isPhoneNumberValid(this.phoneNumber2) -> ClientUIError.PHONE_NUMBER_2_IS_INVALID
        this.phoneNumber1 == this.phoneNumber2 -> ClientUIError.PHONE_NUMBER_2_IS_IN_USE
        this.email.isNotBlank() && !InputValidator.isEmailValid(this.email) -> ClientUIError.EMAIL_IS_INVALID
        customers.any { it.phoneNumber1 == this.phoneNumber1 || this.phoneNumber1 == it.phoneNumber2} -> ClientUIError.PHONE_NUMBER_1_IS_IN_USE
        customers.any { it.phoneNumber2 == this.phoneNumber2 || this.phoneNumber2 == it.phoneNumber1} -> ClientUIError.PHONE_NUMBER_2_IS_IN_USE
        else -> ClientUIError.NONE
    }
}

@Composable
private fun ColumnScope.SelectCustomerOptionSection(
    onOptionSelected: (Boolean) -> Unit = {},
) {
    Surface (
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            onOptionSelected(false)
        },
        contentColor = MaterialTheme.colorScheme.onSurface,
        color = Color.Transparent
    ){
        Row (
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ){
            Icon(
                imageVector = Icons.Default.Person,
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = "Select existing customer",
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = stringResource(R.string.select_existing_customer),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
    Surface (
        modifier = Modifier.fillMaxWidth(),
        onClick = {onOptionSelected(true)},
        contentColor = MaterialTheme.colorScheme.onSurface,
        color = Color.Transparent
    ){
        Row (
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ){
            Icon(
                imageVector = Icons.Default.PersonAdd,
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = "Add new customer",
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = stringResource(R.string.add_new_customer),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
