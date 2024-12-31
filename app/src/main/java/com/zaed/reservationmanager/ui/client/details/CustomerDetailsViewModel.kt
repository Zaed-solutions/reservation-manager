package com.zaed.reservationmanager.ui.client.details

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.data.repository.CompanyRepository
import com.zaed.reservationmanager.data.repository.CustomerRepository
import com.zaed.reservationmanager.data.repository.EmployeeRepository
import com.zaed.reservationmanager.data.repository.ReservationRepository
import com.zaed.reservationmanager.ui.dropdownmenu.MenuDataStore
import com.zaed.reservationmanager.ui.util.Constants.CAR_TYPES_KEY
import com.zaed.reservationmanager.ui.util.Constants.RESERVATION_TYPES_KEY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CustomerDetailsViewModel(
    private val reservationRepo: ReservationRepository,
    private val customerRepo: CustomerRepository,
    private val employeeRepo: EmployeeRepository,
    private val companyRepo: CompanyRepository,
    private val menuDataStore: MenuDataStore
) : ViewModel() {
    private val _uiState = MutableStateFlow(CustomerDetailsUiState())
    val uiState = _uiState.asStateFlow()
    fun init(customerId: String) {
        fetchCustomer(customerId)
        fetchCustomerReservations(customerId)
        fetchReservationTypes()
        fetchCarTypes()
        fetchTravelCompanies()
        fetchTourismCompanies()
    }

    private fun fetchReservationTypes() {
        viewModelScope.launch(Dispatchers.IO) {
            menuDataStore.getMenus(RESERVATION_TYPES_KEY).collect { data ->
                _uiState.update { oldState ->
                    oldState.copy(
                        reservationTypes = data.toList()
                    )
                }
            }
        }
    }

    private fun fetchCarTypes() {
        viewModelScope.launch(Dispatchers.IO) {
            menuDataStore.getMenus(CAR_TYPES_KEY).collect { data ->
                _uiState.update { oldState ->
                    oldState.copy(
                        cars = data.toList()
                    )
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

    private fun fetchCustomerReservations(customerId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            reservationRepo.getReservationByCustomerId(customerId).collect { result ->
                result.onSuccess { data ->
                    _uiState.update { oldState ->
                        oldState.copy(reservations = data.sortedBy { it.date })
                    }
                }.onFailure {
                    Log.e(TAG, "fetchCustomerReservations: failed")
                    it.printStackTrace()
                }
            }
        }
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

    fun handleAction(action: CustomerDetailsUiAction) {
        when (action) {
            is CustomerDetailsUiAction.OnDeleteReservation -> deleteReservation(action.reservationId)
            is CustomerDetailsUiAction.OnAddReservation -> addReservation(action.reservation, action.onSuccess)
            is CustomerDetailsUiAction.OnFetchDrivers -> fetchDrivers(action.companyId)
            is CustomerDetailsUiAction.OnFetchEmployees -> fetchEmployees(action.companyId)
            is CustomerDetailsUiAction.OnUpdateReservation -> updateReservation(action.reservation, action.onSuccess)
            is CustomerDetailsUiAction.ReservationInfoSent -> updateReservation(
                action.reservationId,
                mapOf("sentDriverInfoToCustomer" to true)
            )

            is CustomerDetailsUiAction.ReservationConfirmationSent -> updateReservation(
                action.reservationId,
                mapOf("sentConfirmToCustomer" to true)
            )

            is CustomerDetailsUiAction.ReservationInfoToTravelCompanySent -> updateReservation(
                action.reservationId,
                mapOf("sentToDriverCompany" to true)
            )

            is CustomerDetailsUiAction.ThanksMessageSent -> updateReservation(
                action.reservationId,
                mapOf("sentThanksToCustomer" to true)
            )

            is CustomerDetailsUiAction.ArchiveReservation -> {
                updateReservation(
                    action.reservationId,
                    hashMapOf("archived" to true)
                )
            }

            else -> Unit
        }
    }

    private fun updateReservation(reservationId: String, updates: Map<String, Any>) {
        viewModelScope.launch(Dispatchers.IO) {
            reservationRepo.updateReservation(
                reservationId,
                updates
            ).collect { result ->
                result.onSuccess {
                    Log.d("DisplayReservationViewModel", "sendInfoToTravelCompany: success")
                }.onFailure { e ->
                    Log.e(
                        "DisplayReservationViewModel",
                        "sendInfoToTravelCompany: ${e.message}"
                    )
                    e.printStackTrace()
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

    private fun updateReservation(reservation: Reservation, onSuccess: () -> Unit) {
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

    private fun addReservation(reservation: Reservation, onSuccess: ()-> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val customer = uiState.value.customer
            reservationRepo.createReservation(
                reservation.copy(
                    clientId = customer.id,
                    clientName = customer.name,
                    clientCountry = customer.residenceCountry,
                    clientPhone = customer.phoneNumber
                )
            ).collect { result ->
                result.onSuccess {
                    Log.d(TAG, "addReservation: success")
                    onSuccess()
                }.onFailure { e ->
                    Log.e(TAG, "addReservation: failed to add reservation: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    private fun deleteReservation(reservationId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            reservationRepo.deleteReservation(reservationId).collect { result ->
                result.onSuccess {
                    Log.d(TAG, "deleteReservation: success")
                }.onFailure { e ->
                    Log.e(TAG, "deleteReservation: failed to delete reservation: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    companion object {
        private const val TAG = "CustomerDetailsVM"
    }
}