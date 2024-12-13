package com.zaed.reservationmanager.ui.reservation.display

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaed.reservationmanager.data.repository.ReservationRepository
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


}