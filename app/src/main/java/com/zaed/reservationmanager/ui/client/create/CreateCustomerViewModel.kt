package com.zaed.reservationmanager.ui.client.create

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.data.repository.CustomerRepository
import com.zaed.reservationmanager.data.repository.Menus
import com.zaed.reservationmanager.data.repository.MenusDataRepository
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
    private val menusDataRepository: MenusDataRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(NewClientUiState())
    val uiState = _uiState.asStateFlow()

    fun init(initialCustomer: Customer) {
        Log.d(TAG, "init: $initialCustomer")
        _uiState.update {
            it.copy(isNew = initialCustomer.id.isBlank(), customer = initialCustomer)
        }
        fetchCountries()
    }

    private fun fetchCountries() {
        viewModelScope.launch {
            menusDataRepository.getMenuByName(Menus.COUNTRIES).collect { result ->
                result.onSuccess {menu->
                    _uiState.update { oldState ->
                        oldState.copy(
                            countries = menu.data,
                            nationalities = menu.data
                        )
                    }
                }

            }
        }
    }

    private fun onSubmit() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(loading = true)
            }
            with(uiState.value) {
                if (customer.name.isBlank()) {
                    _uiState.update {
                        it.copy(
                            error = ClientUIError.NAME_IS_REQUIRED,
                            loading = false
                        )
                    }
                    return@launch
                }
                if (customer.phoneNumber1.isBlank()) {
                    _uiState.update {
                        it.copy(
                            error = ClientUIError.PHONE_NUMBER_IS_REQUIRED,
                            loading = false
                        )
                    }
                    return@launch
                }
                if (!InputValidator.isPhoneNumberValid(customer.phoneNumber1)) {
                    _uiState.update {
                        it.copy(
                            error = ClientUIError.PHONE_NUMBER_1_IS_INVALID,
                            loading = false
                        )
                    }
                    return@launch
                }
                if (customer.phoneNumber2.isNotBlank() && !InputValidator.isPhoneNumberValid(
                        customer.phoneNumber2
                    )
                ) {
                    _uiState.update {
                        it.copy(
                            error = ClientUIError.PHONE_NUMBER_2_IS_INVALID,
                            loading = false
                        )
                    }
                    return@launch
                }
                if(customer.phoneNumber1 == customer.phoneNumber2){
                    _uiState.update {
                        it.copy(
                            error = ClientUIError.PHONE_NUMBER_2_IS_IN_USE,
                            loading = false
                        )
                    }
                    return@launch
                }
                if (customer.email.isNotBlank() && !InputValidator.isEmailValid(customer.email)) {
                    _uiState.update {
                        it.copy(
                            error = ClientUIError.EMAIL_IS_INVALID,
                            loading = false
                        )
                    }
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
        Log.d(TAG, "updateClient: called: ${uiState.value.customer}")
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCustomer(uiState.value.customer).collect { result ->
                Log.d(TAG, "updateClient: $result")
                result.onSuccess { data ->
                    Log.d(TAG, "updateClient: success: $data")
                    if (data.first) {
                        _uiState.update { oldState ->
                            oldState.copy(successStatus = true, loading = false)
                        }
                        Log.d(TAG, "addClient: SUCCESS")
                    } else {
                        if (data.second == "phoneNumber1") {
                            _uiState.update { oldState ->
                                oldState.copy(
                                    error = ClientUIError.PHONE_NUMBER_1_IS_IN_USE,
                                    loading = false
                                )
                            }
                            Log.d(TAG, "addClient: PHONE_NUMBER_ALREADY_EXISTS")
                        } else {
                            _uiState.update { oldState ->
                                oldState.copy(
                                    error = ClientUIError.PHONE_NUMBER_2_IS_IN_USE,
                                    loading = false
                                )
                            }
                            Log.d(TAG, "addClient: PHONE_NUMBER_ALREADY_EXISTS")
                        }

                    }
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
        Log.d(TAG, "createClient: called: ${uiState.value.customer}")
        viewModelScope.launch(Dispatchers.IO) {
            repository.createCustomer(uiState.value.customer.copy(createdAtEpochSeconds = Clock.System.now().epochSeconds))
                .collect { result ->
                    result.onSuccess { data ->
                        if (data.first) {
                            _uiState.update { oldState ->
                                oldState.copy(successStatus = true, loading = false)
                            }
                            Log.d(TAG, "addClient: SUCCESS")
                        } else {
                            if (data.second == "phoneNumber1") {
                                _uiState.update { oldState ->
                                    oldState.copy(
                                        error = ClientUIError.PHONE_NUMBER_1_IS_IN_USE,
                                        loading = false
                                    )
                                }
                            } else {
                                _uiState.update { oldState ->
                                    oldState.copy(
                                        error = ClientUIError.PHONE_NUMBER_2_IS_IN_USE,
                                        loading = false
                                    )
                                }
                            }

                            Log.d(TAG, "addClient: PHONE_NUMBER_ALREADY_EXISTS")
                        }
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

    private fun updateMobile1(mobile1: String) {
        _uiState.update { oldState ->
            oldState.copy(
                customer = oldState.customer.copy(phoneNumber1 = mobile1)
            )
        }
    }

    private fun updateMobile2(mobile2: String) {
        _uiState.update { oldState ->
            oldState.copy(
                customer = oldState.customer.copy(phoneNumber2 = mobile2)
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
            CreateCustomerUiAction.SubmitClient -> onSubmit()
            is CreateCustomerUiAction.UpdateCountry -> updateCountryOfResidence(action.country)
            is CreateCustomerUiAction.UpdateEmail -> updateEmail(action.email)
            is CreateCustomerUiAction.UpdateName -> updateClientName(action.name)
            is CreateCustomerUiAction.UpdateNationality -> updateNationality(action.nationality)
            is CreateCustomerUiAction.UpdateNumber1 -> updateMobile1(action.number)
            is CreateCustomerUiAction.UpdateNumber2 -> updateMobile2(action.number)
            is CreateCustomerUiAction.UpdateCity -> updateCity(action.city)
            is CreateCustomerUiAction.UpdateJob -> updateJob(action.job)
        }
    }

    private fun updateCity(city: String) {
        viewModelScope.launch {
            _uiState.update { oldState ->
                oldState.copy(
                    customer = oldState.customer.copy(city = city)
                )
            }
        }
    }

    private fun updateJob(job: String) {
        viewModelScope.launch {
            _uiState.update { oldState ->
                oldState.copy(
                    customer = oldState.customer.copy(job = job)
                )
            }
        }
    }


}