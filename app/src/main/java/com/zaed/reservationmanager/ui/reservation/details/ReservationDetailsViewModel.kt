package com.zaed.reservationmanager.ui.reservation.details

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaed.reservationmanager.data.model.Ride
import com.zaed.reservationmanager.data.repository.CompanyRepository
import com.zaed.reservationmanager.data.repository.EmployeeRepository
import com.zaed.reservationmanager.data.repository.ReservationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReservationDetailsViewModel(
    private val reservationRepo: ReservationRepository,
    private val companyRepo: CompanyRepository,
    private val employeeRepo: EmployeeRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReservationDetailsUiState())
    val uiState = _uiState.asStateFlow()
    val TAG = "ReservationDetailsVM"
    fun init(reservationId: String) {
        fetchReservation(reservationId)
        fetchRides(reservationId)
        fetchTravelCompanies()
    }

    private fun fetchTravelCompanies() {
        viewModelScope.launch(Dispatchers.IO) {
            companyRepo.getTravelCompanies().collect { result ->
                result.onSuccess { data ->
                    Log.d(TAG, "fetchTravelCompanies: success")
                    _uiState.update { oldState ->
                        oldState.copy(travelCompanies = data)
                    }
                }.onFailure { e ->
                    Log.e(TAG, "fetchTravelCompanies: failed to fetch: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    private fun fetchReservation(reservationId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            reservationRepo.getReservationById(reservationId).collect { result ->
                result.onSuccess { data ->
                    Log.d(TAG, "fetchReservation: success")
                    _uiState.update { oldState ->
                        oldState.copy(reservation = data)
                    }
                }.onFailure { e ->
                    Log.e(TAG, "fetchReservation: failed to fetch: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    private fun fetchRides(reservationId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            reservationRepo.getRidesByReservationId(reservationId).collect { result ->
                result.onSuccess { data ->
                    Log.d(TAG, "fetchRides: success")
                    _uiState.update { oldState ->
                        oldState.copy(rides = data)
                    }
                }.onFailure { e ->
                    Log.e(TAG, "fetchRides: failed to fetch: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    fun handleAction(action: ReservationDetailsUiAction) {
        when (action) {
            is ReservationDetailsUiAction.OnDeleteRide -> deleteRide(action.rideId)
            is ReservationDetailsUiAction.OnAddRide -> addRide(action.ride.copy(clientName = uiState.value.reservation.clientName))
            is ReservationDetailsUiAction.UpdateDrivers -> fetchDrivers(action.companyId)
            ReservationDetailsUiAction.OnConfirmationMessageSent -> updateSentConfirmationMessage()
            is ReservationDetailsUiAction.OnDriverInfoSent -> updateSentDriverInfo(action.rideId)
            is ReservationDetailsUiAction.OnInfoSentToTravelCompany -> updateSentInfoToTravelCompany(
                action.rideId
            )

            else -> Unit
        }
    }

    private fun updateSentInfoToTravelCompany(rideId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "updateSentInfoToTravelCompany: $rideId")
            reservationRepo.updateRide(rideId, hashMapOf("sentToDriverCompany" to true))
                .collect { result ->
                    result.onSuccess {
                        Log.d(TAG, "updateSentInfoToTravelCompany: success")
                        _uiState.update { oldState ->
                            oldState.copy(rides = oldState.rides.map { ride ->
                                if (ride.id == rideId) ride.copy(sentToDriverCompany = true)
                                else ride
                            }
                            )
                        }
                    }.onFailure { e ->
                        Log.e(TAG, "updateSentInfoToTravelCompany: failed to update: ${e.message}")
                        e.printStackTrace()
                    }
                }
        }
    }

    private fun updateSentConfirmationMessage() {
        viewModelScope.launch(Dispatchers.IO) {
            reservationRepo.updateReservation(
                uiState.value.reservation.id,
                hashMapOf("sentConfirmToCustomer" to true)
            ).collect { result ->
                result.onSuccess {
                    Log.d(TAG, "updateSentConfirmationMessage: success")
                    _uiState.update { oldState ->
                        oldState.copy(reservation = oldState.reservation.copy(sentConfirmToCustomer = true))
                    }
                }.onFailure { e ->
                    Log.e(TAG, "updateSentConfirmationMessage: failed to update: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    private fun updateSentDriverInfo(rideId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            reservationRepo.updateRide(rideId, hashMapOf("sentDriverInfoToCustomer" to true))
                .collect { result ->
                    result.onSuccess {
                        Log.d(TAG, "updateSentDriverInfo: success")
                        _uiState.update { oldState ->
                            oldState.copy(rides = oldState.rides.map { ride ->
                                if (ride.id == rideId) ride.copy(sentDriverInfoToCustomer = true)
                                else ride
                            })
                        }
                    }.onFailure { e ->
                        Log.e(TAG, "updateSentDriverInfo: failed to update: ${e.message}")
                        e.printStackTrace()
                    }
                }
        }
    }

    private fun fetchDrivers(companyId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            employeeRepo.getEmployeesByCompany(companyId).collect { result ->
                result.onSuccess { data ->
                    Log.d(TAG, "fetchDrivers: success")
                    _uiState.update { oldState ->
                        oldState.copy(drivers = data)
                    }
                }.onFailure { e ->
                    Log.e(TAG, "fetchDrivers: failed to fetch: ${e.message}")
                    e.printStackTrace()
                }
            }
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

    private fun addRide(ride: Ride) {
        viewModelScope.launch(Dispatchers.IO) {
            reservationRepo.createRide(ride.copy(reservationId = uiState.value.reservation.id))
                .collect { result ->
                    result.onSuccess { rideId ->
                        Log.d(TAG, "addRide: success")
                        _uiState.update { oldState ->
                            oldState.copy(rides = oldState.rides + ride.copy(id = rideId))
                        }
                    }.onFailure { e ->
                        Log.e(TAG, "addRide: failed to add: ${e.message}")
                        e.printStackTrace()
                    }
                }
        }
    }
}