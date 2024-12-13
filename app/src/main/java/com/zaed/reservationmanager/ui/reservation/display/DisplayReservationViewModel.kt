package com.zaed.reservationmanager.ui.reservation.display

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaed.reservationmanager.data.repository.ReservationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DisplayReservationViewModel(
    val repository: ReservationRepository
) : ViewModel() {
    private val _state = MutableStateFlow(DisplayReservationState())
    val state = _state.asStateFlow()

    init {
        fetchRides()
        fetchReservations()
    }

    private fun fetchReservations() {
        viewModelScope.launch {
            repository.getReservations().collect{results->
                results.onSuccess {data->
                    _state.update {
                        it.copy(reservations = data)
                    }
                    }.onFailure {
                    Log.d("DisplayReservationViewModel", "fetchReservations: ${it.message}")
                }
            }
        }
    }

    private fun fetchRides() {
        viewModelScope.launch {
            repository.getRides().collect{results->
                results.onSuccess {data->
                    _state.update {
                        it.copy(rides = data)
                    }
                }.onFailure {
                    Log.d("DisplayReservationViewModel", "fetchRides: ${it.message}")
                }
            }
        }
    }

    fun handleAction(displayReservationUIAction: DisplayReservationUIAction) {
        when (displayReservationUIAction) {
            is DisplayReservationUIAction.OnDeleteRide -> deleteRide(displayReservationUIAction.rideId)
            is DisplayReservationUIAction.OnDriverInfoSent -> sendDriverInfo(displayReservationUIAction.rideId)
            is DisplayReservationUIAction.OnInfoSentToTravelCompany -> sendInfoToTravelCompany(displayReservationUIAction.rideId)
            is DisplayReservationUIAction.OnDeleteReservation -> onDeleteReservation(displayReservationUIAction.reservationId)
        }
    }

    private fun onDeleteReservation(reservationId: String) {
        viewModelScope.launch {
            repository.deleteReservation(reservationId).collect {
                it.onSuccess {
                    Log.d("DisplayReservationViewModel", "onDeleteReservation: success")
                }.onFailure {
                    Log.d("DisplayReservationViewModel", "onDeleteReservation: ${it.message}")
                }
            }
        }
    }

    private fun sendInfoToTravelCompany(rideId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateRide(rideId, hashMapOf("sentToDriverCompany" to true))
                .collect { result ->
                    result.onSuccess {
                        Log.d("DisplayReservationViewModel", "sendInfoToTravelCompany: success")
                    }.onFailure { e ->
                        Log.e("DisplayReservationViewModel", "sendInfoToTravelCompany: ${e.message}")
                        e.printStackTrace()
                    }
                }
        }
    }

    private fun sendDriverInfo(rideId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateRide(rideId, hashMapOf("sentDriverInfoToCustomer" to true))
                .collect { result ->
                    result.onSuccess {
                        Log.d("DisplayReservationViewModel", "sendDriverInfo: success")
                    }.onFailure { e ->
                        e.printStackTrace()
                        Log.e("DisplayReservationViewModel", "sendDriverInfo: ${e.message}")
                    }
                }
        }
    }

    private fun deleteRide(rideId: String) {
        viewModelScope.launch {
            repository.deleteRide(rideId).collect{
                it.onSuccess {
                    Log.d("DisplayReservationViewModel", "deleteRide: success")
                }.onFailure {
                    Log.d("DisplayReservationViewModel", "deleteRide: ${it.message}")
                }
            }
        }
    }


}