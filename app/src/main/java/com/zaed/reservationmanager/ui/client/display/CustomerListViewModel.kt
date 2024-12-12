package com.zaed.reservationmanager.ui.client.display

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.data.repository.CustomerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CustomerListViewModel(
    private val customerRepository: CustomerRepository
) : ViewModel() {
    val _state = MutableStateFlow(CustomerListUiState())
    val state = _state.asStateFlow()

    init {
        fetchCustomers()
    }

    private fun fetchCustomers() {
        viewModelScope.launch {
            customerRepository.getCustomers().collect{result->
                result.onSuccess { data ->
                    _state.update { oldState ->
                        oldState.copy(customers = data.sortedBy { it.createdAtEpochSeconds })
                    }
                }.onFailure {
                    _state.value = _state.value.copy(errorMessage = it.message ?: "Unknown error")
                }
            }
        }
    }
    fun deleteCustomer(customerId: String) {
        viewModelScope.launch {
            customerRepository.deleteCustomer(customerId).collect{result->
                result.onSuccess {
                    Log.d("CustomerListViewModel", "Customer deleted successfully")
                }.onFailure {
                    _state.value = _state.value.copy(errorMessage = it.message ?: "Unknown error")
                }
            }
        }
    }



}