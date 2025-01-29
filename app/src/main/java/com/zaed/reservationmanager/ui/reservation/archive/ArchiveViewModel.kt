package com.zaed.reservationmanager.ui.reservation.archive

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.data.repository.CompanyRepository
import com.zaed.reservationmanager.data.repository.EmployeeRepository
import com.zaed.reservationmanager.data.repository.Menus
import com.zaed.reservationmanager.data.repository.MenusDataRepository
import com.zaed.reservationmanager.data.repository.ReservationRepository
import com.zaed.reservationmanager.ui.company.details.CompanyDetailsViewModel
import com.zaed.reservationmanager.ui.company.details.CompanyDetailsViewModel.Companion
import com.zaed.reservationmanager.ui.util.Constants.CAR_TYPES_KEY
import com.zaed.reservationmanager.ui.util.Constants.RESERVATION_TYPES_KEY
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
    private val reservationRepo: ReservationRepository,
    private val employeeRepo: EmployeeRepository,
    private val menusDataRepository: MenusDataRepository,
    private val companyRepo: CompanyRepository,
    ): ViewModel() {
    private val _uiState = MutableStateFlow(ArchiveUiState())
    val uiState = _uiState
        .onStart {
            fetchReservations()
            fetchReservationTypes()
            fetchCarTypes()
            fetchTravelCompanies()
            fetchTourismCompanies()
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
                        oldState.copy(reservations = data.sortedBy { it.date+it.time })
                    }
                }.onFailure { e ->
                    Log.e(TAG, "fetchReservations: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    private fun fetchReservationTypes() {
        viewModelScope.launch(Dispatchers.IO) {
            menusDataRepository.getMenuByName(Menus.RESERVATION_TYPES).collect { result ->
                result.onSuccess { menu ->
                    _uiState.update { oldState ->
                        oldState.copy(
                            reservationTypes = menu.data,
                        )
                    }
                }
            }
        }
    }

    private fun fetchCarTypes() {
        viewModelScope.launch(Dispatchers.IO) {
            menusDataRepository.getMenuByName(Menus.CAR_TYPES).collect { result ->
                result.onSuccess { menu ->
                    _uiState.update { oldState ->
                        oldState.copy(
                            cars = menu.data,
                        )
                    }
                }
            }
        }
    }

    private fun fetchTravelCompanies() {
        viewModelScope.launch(Dispatchers.IO) {
            companyRepo.getCompanies(isDriver = true).collect { result ->
                result.onSuccess { companies ->
                    _uiState.update {
                        it.copy(
                            travelCompanies = companies
                        )
                    }
                }.onFailure {
                    Log.e(TAG, "fetchTravelCompanies: failed")
                    it.printStackTrace()
                }
            }
        }
    }

    private fun fetchTourismCompanies() {
        viewModelScope.launch(Dispatchers.IO) {
            companyRepo.getCompanies(isDriver = false).collect { result ->
                result.onSuccess { companies ->
                    _uiState.update {
                        it.copy(
                            tourismCompanies = companies
                        )
                    }
                }.onFailure {
                    Log.e(TAG, "fetchTourismCompanies: failed")
                    it.printStackTrace()
                }
            }
        }
    }

    fun handleAction(action: ArchiveUiAction) {
        when(action){
            is ArchiveUiAction.DeleteReservation -> deleteReservation(action.reservation)
            is ArchiveUiAction.UnarchiveReservation -> unarchiveReservation(action.reservationId)
            is ArchiveUiAction.OnFetchDrivers -> fetchDrivers(action.companyId)
            is ArchiveUiAction.OnFetchEmployees -> fetchEmployees(action.companyId)
            is ArchiveUiAction.OnAddReservation -> addReservation(action.reservation, action.onSuccess)
            is ArchiveUiAction.OnEditReservation -> editReservation(action.reservation, action.onSuccess)
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

    private fun deleteReservation(reservation: Reservation) {
        viewModelScope.launch (Dispatchers.IO){
            reservationRepo.deleteReservation(reservation).collect {
                it.onSuccess {
                    Log.d("DisplayReservationViewModel", "onDeleteReservation: success")
                }.onFailure {
                    Log.d("DisplayReservationViewModel", "onDeleteReservation: ${it.message}")
                }
            }
        }
    }
    private fun fetchDrivers(companyId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            employeeRepo.getEmployeesByCompany(companyId).collect { result ->
                result.onSuccess { data ->
                    _uiState.update { oldState ->
                        oldState.copy(drivers = data)
                    }
                }.onFailure { e ->
                    Log.e(TAG, "fetchDrivers: failed to fetch drivers: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    private fun fetchEmployees(companyId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            employeeRepo.getEmployeesByCompany(companyId).collect { result ->
                result.onSuccess { data ->
                    _uiState.update { oldState ->
                        oldState.copy(employees = data)
                    }
                }.onFailure { e ->
                    Log.e(TAG, "fetchDrivers: failed to fetch drivers: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    private fun addReservation(reservation: Reservation, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            reservationRepo.createReservation(reservation).collect { result ->
                result.onSuccess {
                    Log.d(TAG, "addReservations: success")
                    onSuccess()
                }.onFailure { e ->
                    Log.e(TAG, "addReservations: failed to add reservation: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    private fun editReservation(reservation: Reservation, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            reservationRepo.updateReservation(reservation).collect { result ->
                result.onSuccess {
                    Log.d(TAG, "updateReservations: success")
                    onSuccess()
                }.onFailure { e ->
                    Log.e(TAG, "updateReservations: failed to update reservation: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }
}