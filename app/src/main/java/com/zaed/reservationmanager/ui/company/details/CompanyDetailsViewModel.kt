package com.zaed.reservationmanager.ui.company.details

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaed.reservationmanager.data.model.CompanyType
import com.zaed.reservationmanager.data.model.Customer
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

class CompanyDetailsViewModel(
    private val reservationRepo: ReservationRepository,
    private val companyRepo: CompanyRepository,
    private val customerRepo: CustomerRepository,
    private val employeeRepo: EmployeeRepository,
    private val menuDataStore: MenuDataStore
) : ViewModel() {
    private val _uiState = MutableStateFlow(CompanyDetailsUiState())
    val uiState = _uiState.asStateFlow()

    companion object {
        private const val TAG = "CompanyDetailsViewModel"
    }

    fun init(companyId: String, companyType: CompanyType) {
        fetchCompany(companyId)
        fetchBalance(companyId, companyType)
        fetchReservations(companyId)
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

    private fun fetchReservations(companyId: String) {
        viewModelScope.launch(Dispatchers.IO) {
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

    private fun fetchBalance(companyId: String, companyType: CompanyType) {
        viewModelScope.launch(Dispatchers.IO) {
            reservationRepo.getCompanyBalance(companyId, companyType).onSuccess {
                _uiState.update { oldState ->
                    oldState.copy(balance = it)
                }
                Log.d(TAG, "fetchBalance: $it")
            }.onFailure { e ->
                Log.e(TAG, "fetchBalance: failed to fetch balance: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun fetchCompany(companyId: String) {
        viewModelScope.launch(Dispatchers.IO) {
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

    fun handleAction(action: CompanyDetailsUiAction) {
        when (action) {
            is CompanyDetailsUiAction.OnDeleteReservation -> deleteReservation(action.reservationId)
            is CompanyDetailsUiAction.OnEditReservation -> editReservation(
                action.reservation,
                action.onSuccess
            )

            is CompanyDetailsUiAction.OnFetchDrivers -> fetchDrivers(action.companyId)
            is CompanyDetailsUiAction.OnFetchEmployees -> fetchEmployees(action.companyId)
            is CompanyDetailsUiAction.ReservationInfoSent -> updateReservation(
                action.reservationId,
                mapOf("sentDriverInfoToCustomer" to true)
            )

            is CompanyDetailsUiAction.FetchCustomerForUpdating -> fetchCustomer(
                action.customerId,
                action.onSuccess
            )

            is CompanyDetailsUiAction.ReservationConfirmationSent -> updateReservation(
                action.reservationId,
                mapOf("sentConfirmToCustomer" to true)
            )

            is CompanyDetailsUiAction.ReservationInfoToTravelCompanySent -> updateReservation(
                action.reservationId,
                mapOf("sentToDriverCompany" to true)
            )

            is CompanyDetailsUiAction.ArchiveReservation -> {
                updateReservation(
                    action.reservationId,
                    hashMapOf("archived" to true)
                )
            }

            else -> Unit
        }
    }

    private fun fetchCustomer(customerId: String, onSuccess: (Customer) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            customerRepo.getCustomerById(customerId).onSuccess {
                onSuccess(it)
            }.onFailure { e ->
                Log.e(TAG, "fetchCustomer: failed to fetch customer: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun updateReservation(reservationId: String, updates: Map<String, Any>) {
        viewModelScope.launch(Dispatchers.IO) {
            reservationRepo.updateReservation(
                reservationId,
                updates
            ).collect { result ->
                result.onSuccess {
                    Log.d(TAG, "sendInfoToTravelCompany: success")
                }.onFailure { e ->
                    Log.e(
                        TAG,
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

    private fun deleteReservation(reservationId: String) {
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