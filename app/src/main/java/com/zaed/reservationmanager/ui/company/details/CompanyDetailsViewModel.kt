package com.zaed.reservationmanager.ui.company.details

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaed.reservationmanager.data.repository.CompanyRepository
import com.zaed.reservationmanager.data.repository.ReservationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CompanyDetailsViewModel(
    private val reservationRepo: ReservationRepository,
    private val companyRepo: CompanyRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(CompanyDetailsUiState())
    val uiState = _uiState.asStateFlow()
    companion object {
        private const val TAG = "CompanyDetailsViewModel"
    }
    fun init(companyId: String, isTravel: Boolean){
        fetchCompany(companyId)
        fetchBalance(companyId, isTravel)
        if(isTravel){
            fetchRides(companyId)
        } else {
            fetchReservations(companyId)
        }
    }

    private fun fetchReservations(companyId: String) {
        viewModelScope.launch (Dispatchers.IO) {
            reservationRepo.getReservationsByCompanyId(companyId).collect { result ->
                result.onSuccess {
                    Log.d(TAG, "fetchReservations: success ${it.size}")
                    _uiState.update { oldState ->
                        oldState.copy(reservations = it)
                    }
                }.onFailure { e ->
                    Log.e(TAG, "fetchReservations: failed to fetch reservations: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    private fun fetchRides(companyId: String) {
        viewModelScope.launch (Dispatchers.IO) {
            reservationRepo.getRidesByCompanyId(companyId).collect { result ->
                result.onSuccess {
                    _uiState.update { oldState ->
                        oldState.copy(rides = it)
                    }
                }.onFailure { e ->
                    Log.e(TAG, "fetchRides: failed to fetch rides: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    private fun fetchBalance(companyId: String, isTravel: Boolean) {
        viewModelScope.launch (Dispatchers.IO) {
            reservationRepo.getCompanyBalance(companyId, isTravel).collect { result ->
                result.onSuccess {
                    _uiState.update { oldState ->
                        oldState.copy(balance = it)
                    }
                }.onFailure { e ->
                    Log.e(TAG, "fetchBalance: failed to fetch balance: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    private fun fetchCompany(companyId: String) {
        viewModelScope.launch (Dispatchers.IO){
            companyRepo.getCompanyById(companyId).collect { result ->
                result.onSuccess {
                    _uiState.update { oldState ->
                        oldState.copy(company = it)
                    }
                }.onFailure { e ->
                    Log.e(TAG, "fetchCompany: failed to fetch company: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    fun handleAction(action: CompanyDetailsUiAction){
        when(action){
            is CompanyDetailsUiAction.OnDeleteRide -> deleteRide(action.rideId)
            is CompanyDetailsUiAction.OnDeleteReservation -> deleteReservation(action.reservationId)
            else -> Unit
        }
    }
    private fun deleteRide(rideId: String) {
        Log.d(TAG, "deleteRide: $rideId")
        viewModelScope.launch(Dispatchers.IO) {
            reservationRepo.deleteRide(rideId).collect { result ->
                result.onSuccess {
                    Log.d(TAG, "deleteRide: success")
                    _uiState.update { oldState ->
                        oldState.copy(rides = oldState.rides.filter { it.id != rideId })
                    }
                }.onFailure { e ->
                    Log.e(TAG, "deleteRide: failed to delete: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }
    private fun deleteReservation(reservationId: String){
        Log.d(TAG, "deleteReservation: $reservationId")
        viewModelScope.launch(Dispatchers.IO) {
            reservationRepo.deleteReservation(reservationId).collect { result ->
                result.onSuccess {
                    Log.d(TAG, "deleteReservation: success")
                    _uiState.update { oldState ->
                        oldState.copy(reservations = oldState.reservations.filter { it.id != reservationId })
                    }
                }.onFailure { e ->
                    Log.e(TAG, "deleteReservation: failed to delete: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }
}