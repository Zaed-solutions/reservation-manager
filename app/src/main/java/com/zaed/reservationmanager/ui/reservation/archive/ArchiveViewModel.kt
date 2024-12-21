package com.zaed.reservationmanager.ui.reservation.archive

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaed.reservationmanager.data.repository.ReservationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.bouncycastle.asn1.x500.style.RFC4519Style.o

class ArchiveViewModel(
    private val reservationRepo: ReservationRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(ArchiveUiState())
    val uiState = _uiState
        .onStart {
            fetchReservations()
        }
        .stateIn(
            scope = viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            ArchiveUiState()
        )

    private val TAG = "ArchiveViewModel"

    private fun fetchReservations(){
        viewModelScope.launch (Dispatchers.IO){
            reservationRepo.getArchivedReservations().collect{ result ->
                result.onSuccess { data ->
                    _uiState.update { oldState ->
                        oldState.copy(reservations = data)
                    }
                }.onFailure { e ->
                    Log.e(TAG, "fetchReservations: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    fun handleAction(action: ArchiveUiAction) {
        when(action){
            is ArchiveUiAction.DeleteReservation -> deleteReservation(action.reservationId)
            is ArchiveUiAction.UnarchiveReservation -> unarchiveReservation(action.reservationId)
            else -> Unit
        }
    }

    private fun unarchiveReservation(reservationId: String) {
        viewModelScope.launch (Dispatchers.IO){
            reservationRepo.updateReservation(reservationId, hashMapOf("archived" to false)).collect{ result ->
                result.onSuccess {
                    Log.d(TAG, "unarchiveReservation: success")
                }.onFailure { e ->
                    Log.e(TAG, "unarchiveReservation: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    private fun deleteReservation(reservationId: String) {
        viewModelScope.launch (Dispatchers.IO){
            reservationRepo.deleteReservation(reservationId).collect {
                it.onSuccess {
                    Log.d("DisplayReservationViewModel", "onDeleteReservation: success")
                }.onFailure {
                    Log.d("DisplayReservationViewModel", "onDeleteReservation: ${it.message}")
                }
            }
        }
    }
}