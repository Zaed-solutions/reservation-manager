package com.zaed.reservationmanager.ui.client.create

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.data.repository.CustomerRepository
import com.zaed.reservationmanager.ui.dropdownmenu.MenuDataStore
import com.zaed.reservationmanager.ui.util.Constants.COUNTRIES_KEY
import com.zaed.reservationmanager.ui.util.InputValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

private const val TAG = "CreateCustomerViewModel"

class CreateCustomerViewModel(
    private val repository: CustomerRepository,
    private val menuDataStore: MenuDataStore
) : ViewModel() {
    private val _uiState = MutableStateFlow(NewClientUiState())
    val uiState = _uiState.asStateFlow()

    fun init(initialCustomer: Customer) {
        _uiState.update {
            it.copy(isNew = initialCustomer.id.isBlank(), customer = initialCustomer)
        }
        fetchCountries()
    }

    private fun fetchCountries() {
        viewModelScope.launch {
            menuDataStore.getMenus(COUNTRIES_KEY).collect { data ->
                _uiState.update { oldState ->
                    oldState.copy(
                        countries = data.toList(),
                        nationalities = data.toList()
                    )
                }
            }
        }
    }

    private fun addClient() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(loading = true)
            }
            with(uiState.value) {
                if (customer.name.isBlank()) {
                    _uiState.update { it.copy(error = ClientUIError.NAME_IS_REQUIRED) }
                    return@launch
                }
                if (customer.phoneNumber.isBlank()) {
                    _uiState.update { it.copy(error = ClientUIError.PHONE_NUMBER_IS_REQUIRED) }
                    return@launch
                }
                if (!InputValidator.isPhoneNumberValid(customer.phoneNumber)) {
                    _uiState.update { it.copy(error = ClientUIError.PHONE_NUMBER_IS_INVALID) }
                    return@launch
                }
                if (customer.email.isNotBlank() && !InputValidator.isEmailValid(customer.email)) {
                    _uiState.update { it.copy(error = ClientUIError.EMAIL_IS_INVALID) }
                    return@launch
                }
                _uiState.update { it.copy(error = ClientUIError.NONE) }
                if (isNew) {
                    createClient()
                } else {
                    updateClient()
                }
            }
        }
    }

    private fun updateClient() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCustomer(uiState.value.customer).collect { result ->
                result.onSuccess {
                    _uiState.update { oldState ->
                        oldState.copy(successStatus = true, loading = false)
                    }
                    Log.d(TAG, "updateClient: SUCCESS")
                }.onFailure { error ->
                    _uiState.update { oldState ->
                        oldState.copy(
                            error = ClientUIError.valueOf(error.message ?: ""),
                            loading = false
                        )
                    }
                    Log.d(TAG, "updateClient: ${error.message}")
                    error.printStackTrace()
                }
            }
        }
    }

    private fun createClient() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.createCustomer(uiState.value.customer.copy(createdAtEpochSeconds = Clock.System.now().epochSeconds))
                .collect { result ->
                    result.onSuccess {
                        _uiState.update { oldState ->
                            oldState.copy(successStatus = true, loading = false)
                        }
                        Log.d(TAG, "addClient: SUCCESS")
                    }.onFailure { error ->
                        _uiState.update { oldState ->
                            oldState.copy(
                                error = ClientUIError.valueOf(error.message ?: ""),
                                loading = false
                            )
                        }
                        Log.d(TAG, "addClient: ${error.message}")
                        error.printStackTrace()
                    }
                }
        }
    }

    private fun resetForm() {
        _uiState.value = NewClientUiState()
    }

    private fun dismissErrorDialog() {
        _uiState.update {
            it.copy(
                error = ClientUIError.NONE
            )
        }
    }

    private fun updateEmail(email: String) {
        _uiState.update { oldState ->
            oldState.copy(
                customer = oldState.customer.copy(email = email)
            )
        }
    }

    private fun updateMobile(mobile: String) {
        _uiState.update { oldState ->
            oldState.copy(
                customer = oldState.customer.copy(phoneNumber = mobile)
            )
        }
    }

    private fun updateCountryOfResidence(residenceCountry: String) {
        _uiState.update { oldState ->
            oldState.copy(
                customer = oldState.customer.copy(residenceCountry = residenceCountry)
            )
        }
    }

    private fun updateNationality(nationality: String) {
        _uiState.update { oldState ->
            oldState.copy(
                customer = oldState.customer.copy(nationality = nationality)
            )
        }
    }

    private fun updateClientName(name: String) {
        _uiState.update { oldState ->
            oldState.copy(
                customer = oldState.customer.copy(name = name)
            )
        }
    }

    fun handleAction(action: CreateCustomerUiAction) {
        when (action) {
            CreateCustomerUiAction.AddClient -> addClient()
            is CreateCustomerUiAction.UpdateCountry -> updateCountryOfResidence(action.country)
            is CreateCustomerUiAction.UpdateEmail -> updateEmail(action.email)
            is CreateCustomerUiAction.UpdateName -> updateClientName(action.name)
            is CreateCustomerUiAction.UpdateNationality -> updateNationality(action.nationality)
            is CreateCustomerUiAction.UpdateNumber -> updateMobile(action.number)
        }
    }
}