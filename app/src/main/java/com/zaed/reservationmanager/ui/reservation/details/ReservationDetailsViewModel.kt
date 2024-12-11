package com.zaed.reservationmanager.ui.reservation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReservationDetailsViewModel(

): ViewModel() {
    private val _uiState = MutableStateFlow(ReservationDetailsUiState())
    val uiState = _uiState.asStateFlow()

    fun init(reservationId: String){
        fetchReservation(reservationId)
    }

    private fun fetchReservation(reservationId: String) {
        viewModelScope.launch(Dispatchers.IO) {

        }
    }

    private fun fetchRides(){
        viewModelScope.launch(Dispatchers.IO) {

        }
    }

    fun handleAction(action: ReservationDetailsUiAction){

    }
}