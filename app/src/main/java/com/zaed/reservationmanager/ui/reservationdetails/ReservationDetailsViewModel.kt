package com.zaed.reservationmanager.ui.reservationdetails

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ReservationDetailsViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(ReservationDetailsUiState())
    val uiState = _uiState.asStateFlow()

    fun init(reservationId: String){
        //todo: fetch reservation details
    }

    fun handleAction(action: ReservationDetailsUiAction){

    }
}