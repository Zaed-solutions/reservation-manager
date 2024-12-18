package com.zaed.reservationmanager.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaed.reservationmanager.data.model.ReservationModel
import com.zaed.reservationmanager.data.repository.CompanyRepository
import com.zaed.reservationmanager.data.repository.CustomerRepository
import com.zaed.reservationmanager.data.repository.EmployeeRepository
import com.zaed.reservationmanager.data.repository.ReservationRepository
import com.zaed.reservationmanager.ui.dropdownmenu.MenuDataStore
import com.zaed.reservationmanager.ui.home.component.TimeFilter
import com.zaed.reservationmanager.ui.util.Constants.CAR_TYPES_KEY
import com.zaed.reservationmanager.ui.util.Constants.RESERVATION_TYPES_KEY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val reservationRepo: ReservationRepository,
    private val customerRepo: CustomerRepository,
    private val employeeRepo: EmployeeRepository,
    private val companyRepo: CompanyRepository,
    private val menuDataStore: MenuDataStore
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchReservations()
        fetchCustomers()
        fetchCountries()
        fetchReservationTypes()
        fetchCars()
        fetchTravelCompanies()
        fetchTourismCompanies()
    }

    private val TAG = "HomeViewModel"
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

    private fun fetchCars() {
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

    private fun fetchCountries() {
//        TODO("Not yet implemented")
    }

    private fun fetchCustomers() {
        viewModelScope.launch {
            customerRepo.getCustomers().collect { result ->
                result.onSuccess { data ->
                    val sortedCustomers = data.sortedBy { it.createdAtEpochSeconds }
                    _uiState.update { oldState ->
                        oldState.copy(
                            customers = sortedCustomers,
                            displayedCustomers = sortedCustomers,
                            countries = sortedCustomers.map { it.residenceCountry }.distinct()
                        )
                    }
                }.onFailure {
                    _uiState.value =
                        _uiState.value.copy(errorMessage = it.message ?: "Unknown error")
                }
            }
        }
    }

    private fun fetchReservations() {
        viewModelScope.launch {
            reservationRepo.getReservations().collect { results ->
                results.onSuccess { data ->
                    _uiState.update {
                        it.copy(reservations = data)
                    }
                }.onFailure {
                    Log.d("DisplayReservationViewModel", "fetchReservations: ${it.message}")
                }
            }
        }
    }

    fun handleAction(action: HomeUiAction) {
        when (action) {
            is HomeUiAction.FetchDrivers -> fetchDrivers(action.companyId)
            is HomeUiAction.FetchEmployees -> fetchEmployees(action.companyId)
            is HomeUiAction.OnConfirmationSentToClient -> updateReservation(
                action.reservationId,
                mapOf("sentConfirmToCustomer" to true)
            )

            is HomeUiAction.OnDeleteCustomer -> deleteCustomer(action.customerId)
            is HomeUiAction.OnDeleteReservation -> deleteReservation(action.reservationId)
            is HomeUiAction.OnDriverInfoSent -> updateReservation(
                action.reservationId,
                mapOf("sentDriverInfoToCustomer" to true)
            )

            is HomeUiAction.OnInfoSentToTravelCompany -> updateReservation(
                action.reservationId,
                mapOf("sentToDriverCompany" to true)
            )

            is HomeUiAction.UpdateCountryFilter -> filterData(
                uiState.value.timeFilter,
                action.countryFilter,
                uiState.value.searchQuery
            )

            is HomeUiAction.UpdateReservation -> updateReservation(action.reservation)
            is HomeUiAction.UpdateSearchQuery -> filterData(
                uiState.value.timeFilter,
                uiState.value.selectedCountry,
                action.query
            )

            is HomeUiAction.UpdateTimeFilter -> filterData(
                action.timeFilter,
                uiState.value.selectedCountry,
                uiState.value.searchQuery
            )

            else -> Unit
        }
    }

    private fun filterData(timeFilter: TimeFilter, countryFilter: String, searchQuery: String) {
        viewModelScope.launch {
            if (searchQuery.isBlank() && countryFilter.isBlank()) {
                _uiState.update {
                    it.copy(
                        displayedCustomers = it.customers,
                        timeFilter = timeFilter,
                        selectedCountry = countryFilter,
                        searchQuery = searchQuery
                    )
                }
            } else if (searchQuery.isBlank()) {
                val filteredCustomers = uiState.value.customers.filter { customer ->
                    listOf(
                        customer.name,
                        customer.phoneNumber
                    ).any { value ->
                        value.contains(searchQuery, ignoreCase = true)
                    } && customer.residenceCountry == countryFilter
                }
                _uiState.update { oldState ->
                    oldState.copy(
                        displayedCustomers = filteredCustomers,
                        timeFilter = timeFilter,
                        selectedCountry = countryFilter,
                        searchQuery = searchQuery
                    )
                }
            } else if (countryFilter.isBlank()) {
                val filteredCustomers = uiState.value.customers.filter { customer ->
                    listOf(
                        customer.name,
                        customer.phoneNumber
                    ).any { value ->
                        value.contains(searchQuery, ignoreCase = true)
                    }
                }
                _uiState.update { oldState ->
                    oldState.copy(
                        displayedCustomers = filteredCustomers,
                        timeFilter = timeFilter,
                        selectedCountry = countryFilter,
                        searchQuery = searchQuery
                    )
                }
            } else {
                val filteredCustomers = uiState.value.customers.filter { customer ->
                    listOf(
                        customer.name,
                        customer.phoneNumber
                    ).any { value ->
                        value.contains(searchQuery, ignoreCase = true)
                    } && customer.residenceCountry == countryFilter
                }
                _uiState.update { oldState ->
                    oldState.copy(
                        displayedCustomers = filteredCustomers,
                        timeFilter = timeFilter,
                        selectedCountry = countryFilter,
                        searchQuery = searchQuery
                    )
                }
            }
            if (searchQuery.isBlank() && timeFilter == TimeFilter.All) {
                _uiState.update {
                    it.copy(
                        displayedReservations = it.reservations,
                        timeFilter = timeFilter,
                        selectedCountry = countryFilter,
                        searchQuery = searchQuery
                    )
                }
            } else if (searchQuery.isBlank()) {
                val filteredReservations = uiState.value.reservations.filter { reservation ->
                    matchesFilter(
                        timeFilter,
                        reservation.date
                    )
                }
                _uiState.update { oldState ->
                    oldState.copy(
                        displayedReservations = filteredReservations,
                        timeFilter = timeFilter,
                        selectedCountry = countryFilter,
                        searchQuery = searchQuery
                    )
                }
            } else if (timeFilter == TimeFilter.All) {
                val filteredReservations = uiState.value.reservations.filter { reservation ->
                    listOf(
                        reservation.clientName,
                        reservation.tourismCompany,
                        reservation.travelCompany,
                        reservation.clientPhone
                    ).any { value ->
                        value.contains(searchQuery, ignoreCase = true)
                    }
                }
                _uiState.update { oldState ->
                    oldState.copy(
                        displayedReservations = filteredReservations,
                        timeFilter = timeFilter,
                        selectedCountry = countryFilter,
                        searchQuery = searchQuery
                    )
                }
            } else {
                val filteredReservations = uiState.value.reservations.filter { reservation ->
                    listOf(
                        reservation.clientName,
                        reservation.tourismCompany,
                        reservation.travelCompany,
                        reservation.clientPhone
                    ).any { value ->
                        value.contains(searchQuery, ignoreCase = true)
                    } && matchesFilter(timeFilter, reservation.date)
                }
                _uiState.update { oldState ->
                    oldState.copy(
                        displayedReservations = filteredReservations,
                        timeFilter = timeFilter,
                        selectedCountry = countryFilter,
                        searchQuery = searchQuery
                    )
                }
            }
        }
    }

    private fun matchesFilter(filter: TimeFilter, epochSeconds: Long): Boolean {
        val epochDate = java.time.Instant.ofEpochSecond(epochSeconds)
            .atZone(java.time.ZoneOffset.UTC)
            .toLocalDate()

        return when (filter) {
            is TimeFilter.All -> true
            is TimeFilter.Yesterday -> epochDate == java.time.LocalDate.now(java.time.ZoneOffset.UTC)
                .minusDays(1)

            is TimeFilter.Today -> epochDate == java.time.LocalDate.now(java.time.ZoneOffset.UTC)
            is TimeFilter.Tomorrow -> epochDate == java.time.LocalDate.now(java.time.ZoneOffset.UTC)
                .plusDays(1)

            is TimeFilter.TodayOnwards -> epochDate >= java.time.LocalDate.now(java.time.ZoneOffset.UTC)
            is TimeFilter.FixedDate -> epochDate == java.time.Instant.ofEpochSecond(filter.date)
                .atZone(java.time.ZoneOffset.UTC).toLocalDate()

            is TimeFilter.FixedRange -> {
                val startDate = java.time.Instant.ofEpochSecond(filter.startDate)
                    .atZone(java.time.ZoneOffset.UTC)
                    .toLocalDate()
                val endDate = java.time.Instant.ofEpochSecond(filter.endDate)
                    .atZone(java.time.ZoneOffset.UTC)
                    .toLocalDate()
                epochDate in startDate..endDate
            }
        }
    }


    fun deleteCustomer(customerId: String) {
        viewModelScope.launch {
            customerRepo.deleteCustomer(customerId).collect { result ->
                result.onSuccess {
                    Log.d("CustomerListViewModel", "Customer deleted successfully")
                }.onFailure {
                    _uiState.update { oldState ->
                        oldState.copy(errorMessage = it.message ?: "Unknown error")
                    }
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

    private fun updateReservation(reservation: ReservationModel) {
        viewModelScope.launch(Dispatchers.IO) {
            reservationRepo.updateReservation(reservation).collect { result ->
                result.onSuccess {
                    Log.d(TAG, "updateReservations: success")
                }.onFailure { e ->
                    Log.e(TAG, "updateReservations: failed to update reservation: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    private fun deleteReservation(reservationId: String) {
        viewModelScope.launch {
            reservationRepo.deleteReservation(reservationId).collect {
                it.onSuccess {
                    Log.d("DisplayReservationViewModel", "onDeleteReservation: success")
                }.onFailure {
                    Log.d("DisplayReservationViewModel", "onDeleteReservation: ${it.message}")
                }
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
}