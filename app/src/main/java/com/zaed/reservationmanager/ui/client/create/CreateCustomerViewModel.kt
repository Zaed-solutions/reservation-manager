package com.zaed.reservationmanager.ui.client.create

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.data.repository.CustomerRepository
import com.zaed.reservationmanager.ui.client.ClientUIError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
private val TAG = "CreateCustomerViewModel"
class CreateCustomerViewModel(
    private val repository: CustomerRepository
) : ViewModel() {
    val _state = MutableStateFlow(NewClientUiState())
    val state = _state.asStateFlow()

    private fun addClient() {
        viewModelScope.launch {
            _state.update {
                it.copy(loading = true)
            }
            if (validateInput()) {
                val newCustomer = Customer(
                    name = _state.value.clientName,
                    nationality = _state.value.nationality,
                    residenceCountry = _state.value.countryOfResidence,
                    phoneNumber = _state.value.mobile,
                    email = _state.value.email,
                )
                repository.createCustomer(newCustomer).collect{result->
                    result.onSuccess {data->
                        _state.update {oldState->
                            oldState.copy(successStatus = true, loading = false)
                        }
                        Log.d(TAG, "addClient: SUCCESS")
                    }.onFailure {error->
                        _state.update {oldState->
                            oldState.copy(errorMessage = ClientUIError.valueOf(error.message?:""), loading = false)
                        }
                        Log.d(TAG, "addClient: ${error.message}")
                        error.printStackTrace()
                    }
                }

            } else {
                _state.update {
                    it.copy(
                        errorMessage = ClientUIError.PLEASE_FILL_IN_ALL_REQUIRED_FIELDS,
                        loading = false
                    )
                }
            }
        }
    }

    fun resetForm() {
        _state.value = NewClientUiState()
    }

    private fun validateInput(): Boolean {
        with(_state.value) {
            if (clientName.isBlank()) {
                _state.update {
                    it.copy(
                        clientNameError = ClientUIError.NAME_IS_REQUIRED
                    )
                }
                return false
            }else{
                _state.update {
                    it.copy(
                        clientNameError = ClientUIError.NONE
                    )
                }
            }
            if (mobile.isBlank()) {
                _state.update {
                    it.copy(
                        mobileError = ClientUIError.PHONE_NUMBER_IS_REQUIRED
                    )
                }
                return false
            }else{
                _state.update {
                    it.copy(
                        mobileError = ClientUIError.NONE
                    )
                }
            }
            if (!isValidMobile(mobile)) {
                _state.update {
                    it.copy(
                        mobileError = ClientUIError.PHONE_NUMBER_IS_INVALID
                    )
                }
                return false
            }else{
                _state.update {
                    it.copy(
                        mobileError = ClientUIError.NONE
                    )
                }
            }
            // Add more validation checks as needed
            return true
        }
    }

    private fun isValidMobile(mobile: String): Boolean {
        // Implement mobile number format validation
        return mobile.matches(Regex("^\\+?\\d{12}$")) // Example: Optional '+' followed by 10 digits
    }

    fun dismissErrorDialog() {
        _state.update {
            it.copy(
                errorMessage = ClientUIError.NONE
            )
        }
    }

    fun updateEmail(email: String) {
        _state.update {
            it.copy(
                email = email
            )
        }
    }

    fun updateMobile(mobile: String) {
        _state.update {
            it.copy(
                mobile = mobile
            )

        }
    }

    fun updateCountryOfResidence(selectionOption: String) {
        _state.update {
            it.copy(
                countryOfResidence = selectionOption
            )
        }
    }

    fun updateNationality(selectionOption: String) {
        _state.update {
            it.copy(
                nationality = selectionOption
            )

        }
    }

    fun updateClientName(name: String) {
        _state.update {
            it.copy(
                clientName = name
            )
        }
    }

    fun handleAction(action: CreateCustomerUiAction) {
        when (action) {
            CreateCustomerUiAction.AddClient -> addClient()
            CreateCustomerUiAction.Cancel -> resetForm()
            CreateCustomerUiAction.DismissStatusError -> dismissErrorDialog()
            is CreateCustomerUiAction.UpdateCountry -> updateCountryOfResidence(action.country)
            is CreateCustomerUiAction.UpdateEmail -> updateEmail(action.email)
            is CreateCustomerUiAction.UpdateName -> updateClientName(action.name)
            is CreateCustomerUiAction.UpdateNationality -> updateNationality(action.nationality)
            is CreateCustomerUiAction.UpdateNumber -> updateMobile(action.number)
        }
    }
}