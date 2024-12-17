package com.zaed.reservationmanager.ui.client.details

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaed.reservationmanager.data.repository.CustomerRepository
import com.zaed.reservationmanager.data.repository.ReservationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CustomerDetailsViewModel(
    private val reservationRepo: ReservationRepository,
    private val customerRepo: CustomerRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CustomerDetailsUiState())
    val uiState = _uiState.asStateFlow()
    fun init(customerId: String) {
        fetchCustomer(customerId)
        fetchCustomerRides(customerId)
    }

    private fun fetchCustomer(customerId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            customerRepo.getCustomerById(customerId).onSuccess { data ->
                _uiState.update { oldState ->
                    oldState.copy(customer = data)
                }
            }.onFailure { e ->
                Log.e(TAG, "fetchCustomer: failed to fetch customer: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun fetchCustomerRides(customerId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            reservationRepo.getRidesByCustomerId(customerId).collect { result ->
                result.onSuccess { data ->
                    _uiState.update { oldState ->
                        oldState.copy(rides = data.sortedByDescending { it.date })
                    }
                }.onFailure { e ->
                    Log.e(TAG, "fetchCustomerRides: failed to fetch customer rides ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    fun handleAction(action: CustomerDetailsUiAction) {
        when (action) {
            is CustomerDetailsUiAction.OnDeleteRide -> deleteRide(action.rideId)
            else -> Unit
        }
    }

    private fun deleteRide(rideId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            reservationRepo.deleteRide(rideId).collect { result ->
                result.onSuccess {
                    _uiState.update { oldState ->
                        oldState.copy(rides = oldState.rides.filter { it.id != rideId })
                    }
                }.onFailure { e ->
                    Log.e(TAG, "deleteRide: failed to delete ride: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }
    companion object {
        private const val TAG = "CustomerDetailsVM"
    }
}