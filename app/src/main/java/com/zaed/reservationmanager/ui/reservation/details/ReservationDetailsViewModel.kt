package com.zaed.reservationmanager.ui.reservation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaed.reservationmanager.data.model.Ride
import com.zaed.reservationmanager.ui.reservation.create.ReservationUiAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReservationDetailsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ReservationDetailsUiState())
    val uiState = _uiState.asStateFlow()

    fun init(reservationId: String) {
        fetchReservation(reservationId)
    }

    private fun fetchReservation(reservationId: String) {
        viewModelScope.launch(Dispatchers.IO) {

        }
    }

    private fun fetchRides() {
        viewModelScope.launch(Dispatchers.IO) {

        }
    }

    fun handleAction(action: ReservationDetailsUiAction) {
        when (action) {
            is ReservationDetailsUiAction.OnDeleteRide -> deleteRide(action.rideId)
            is ReservationDetailsUiAction.OnAddRide -> addRide(action.ride)
            else -> Unit
        }
    }

    private fun deleteRide(rideId: String) {
        TODO("Not yet implemented")
    }

    private fun addRide(ride: Ride) {
        TODO("Not yet implemented")
    }
}