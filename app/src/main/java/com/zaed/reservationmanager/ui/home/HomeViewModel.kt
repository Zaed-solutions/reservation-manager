package com.zaed.reservationmanager.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaed.reservationmanager.data.model.CompanyHistory
import com.zaed.reservationmanager.data.model.CompanyType
import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.data.model.filterOpenAccountCompanies
import com.zaed.reservationmanager.data.repository.CompanyRepository
import com.zaed.reservationmanager.data.repository.CustomerRepository
import com.zaed.reservationmanager.data.repository.EmployeeRepository
import com.zaed.reservationmanager.data.repository.ReservationRepository
import com.zaed.reservationmanager.ui.dropdownmenu.MenuDataStore
import com.zaed.reservationmanager.ui.home.component.Report
import com.zaed.reservationmanager.ui.home.component.TimeFilter
import com.zaed.reservationmanager.ui.util.Constants.CAR_TYPES_KEY
import com.zaed.reservationmanager.ui.util.Constants.COUNTRIES_KEY
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
        viewModelScope.launch(Dispatchers.IO) {
            menuDataStore.getMenus(COUNTRIES_KEY).collect { data ->
                _uiState.update { oldState ->
                    oldState.copy(
                        countries = data.toList()
                    )
                }
            }
        }
    }

    private fun fetchCustomers() {
        viewModelScope.launch(Dispatchers.IO) {
            customerRepo.getCustomers().collect { result ->
                result.onSuccess { data ->
                    _uiState.update { oldState ->
                        oldState.copy(
                            customers = data,
                            displayedCustomers = data
                        )
                    }
                    filterData(countryFilter = uiState.value.selectedCountry)
                }.onFailure {
                    _uiState.value =
                        _uiState.value.copy(errorMessage = it.message ?: "Unknown error")
                }
            }
        }
    }

    private fun fetchReservations() {
        viewModelScope.launch (Dispatchers.IO){
            reservationRepo.getReservations().collect { results ->
                results.onSuccess { data ->
                    _uiState.update {
                        it.copy(reservations = data, displayedReservations = data)
                    }
                    filterData(timeFilter = uiState.value.timeFilter)
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
            is HomeUiAction.AddCustomers -> handleImportedCustomer(action.customers)

            is HomeUiAction.OnDeleteCustomer -> deleteCustomer(action.customerId, action.onShowMessage)
            is HomeUiAction.OnDeleteReservation -> deleteReservation(action.reservationId)
            is HomeUiAction.OnDriverInfoSent -> updateReservation(
                action.reservationId,
                mapOf("sentDriverInfoToCustomer" to true)
            )

            is HomeUiAction.OnInfoSentToTravelCompany -> updateReservation(
                action.reservationId,
                mapOf("sentToDriverCompany" to true)
            )
            is HomeUiAction.ThanksMessageSent -> updateReservation(
                action.reservationId,
                mapOf("sentThanksToCustomer" to true)
            )

            is HomeUiAction.UpdateCountryFilter -> filterData(
                countryFilter = action.countryFilter
            )

            is HomeUiAction.UpdateReservation -> updateReservation(action.reservation, action.onSuccess)
            is HomeUiAction.UpdateSearchQuery -> filterData(
                searchQuery = action.query
            )

            is HomeUiAction.UpdateTimeFilter -> filterData(
                timeFilter = action.timeFilter,
            )
            is HomeUiAction.ArchiveReservation -> {
                updateReservation(
                    action.reservationId,
                    hashMapOf(
                        "archived" to true,
                    )
                )
            }

            is HomeUiAction.FetchReservationsForReport -> fetchReportReservations(action.report, action.onSuccess)
            is HomeUiAction.FetchCompaniesHistory -> fetchCompaniesHistory(action.report, action.onSuccess)

            else -> Unit
        }
    }

    private fun fetchCompaniesHistory(report: Report, onSuccess: (List<CompanyHistory>) -> Unit) {
        viewModelScope.launch (Dispatchers.IO){
            reservationRepo.fetchCompanyOpenAccount(
                report = report
            ).collect{result->
                result.onSuccess { data ->
                    onSuccess(data.filterOpenAccountCompanies(report.companyType?: CompanyType.TRAVEL))
                    Log.d("HomeViewModel", "fetchCompaniesHistory: success $data")
                }.onFailure { e ->
                    e.printStackTrace()
                    Log.e("HomeViewModel", "fetchCompaniesHistory: ${e.message}")
                }
            }
        }
    }


    private fun fetchReportReservations(report: Report, onSuccess: (List<Reservation>) -> Unit) {
        Log.d("ReportTest", "fetchReportReservations: called in vm: $report")
        viewModelScope.launch (Dispatchers.IO){
            reservationRepo.fetchReportReservations(report).collect{ result ->
                result.onSuccess { data ->
                    Log.d("ReportTest", "fetchReportReservations: success ${data.size}")
                    onSuccess(data)
                }.onFailure { e ->
                    Log.e("ReportTest", "fetchReportReservations: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    private fun handleImportedCustomer(customers: List<Customer>) {
        viewModelScope.launch(Dispatchers.Default) {
            val existingCustomers = mutableListOf<Customer>()
            val newCustomers = mutableListOf<Customer>()

            customers.forEach {newCustomer ->
                if (uiState.value.customers.any { it.phoneNumber1 == newCustomer.phoneNumber1 }) {
                    val existingCustomer = uiState.value.customers.first { it.phoneNumber1 == newCustomer.phoneNumber1 }
                    existingCustomers.add(newCustomer.copy(id = existingCustomer.id))
                } else {
                    newCustomers.add(newCustomer)
                }
            }
            if (newCustomers.isNotEmpty()) {
                addCustomers(newCustomers)
            }
            if(existingCustomers.isNotEmpty()){
                updateCustomers(existingCustomers)
            }
        }
    }

    private fun addCustomers(customers: List<Customer>) {
        viewModelScope.launch(Dispatchers.IO) {
            customerRepo.addCustomers(customers).collect { result ->
                result.onSuccess {
                    Log.d("HomeViewModel", "addCustomers: success")
                }.onFailure {
                    Log.e("HomeViewModel", "addCustomers: ${it.message}")
                    it.printStackTrace()
                }
            }
        }
    }
    private fun updateCustomers(customers: List<Customer>) {
        viewModelScope.launch(Dispatchers.IO) {
            customerRepo.updateCustomers(customers).collect { result ->
                result.onSuccess {
                    Log.d("HomeViewModel", "addCustomers: success")
                }.onFailure {
                    Log.e("HomeViewModel", "addCustomers: ${it.message}")
                    it.printStackTrace()
                }
            }
        }
    }

    private fun filterData(
        timeFilter: TimeFilter = uiState.value.timeFilter,
        countryFilter: String = uiState.value.selectedCountry,
        searchQuery: String = uiState.value.searchQuery
    ) {
        Log.d(TAG, "filterData: timeFilter: $timeFilter, currentSeconds: ${java.time.Instant.now().epochSecond}")
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    timeFilter = timeFilter,
                    selectedCountry = countryFilter,
                    searchQuery = searchQuery
                )
            }
        }
        viewModelScope.launch (Dispatchers.Default){
            if (searchQuery.isBlank() && countryFilter.isBlank()) {
                _uiState.update {
                    it.copy(
                        displayedCustomers = it.customers.sortedBy { customer -> customer.name },
                    )
                }
            } else if (searchQuery.isBlank()) {
                val filteredCustomers = uiState.value.customers.filter { customer ->
                    listOf(
                        customer.name,
                        customer.phoneNumber1,
                        customer.phoneNumber2
                    ).any { value ->
                        value.contains(searchQuery, ignoreCase = true)
                    } && customer.residenceCountry == countryFilter
                }.sortedBy { customer -> customer.name }
                _uiState.update { oldState ->
                    oldState.copy(
                        displayedCustomers = filteredCustomers
                    )
                }
            } else if (countryFilter.isBlank()) {
                val filteredCustomers = uiState.value.customers.filter { customer ->
                    listOf(
                        customer.name,
                        customer.phoneNumber1,
                        customer.phoneNumber2
                    ).any { value ->
                        value.contains(searchQuery, ignoreCase = true)
                    }
                }.sortedBy { customer -> customer.name }
                _uiState.update { oldState ->
                    oldState.copy(
                        displayedCustomers = filteredCustomers,
                    )
                }
            } else {
                val filteredCustomers = uiState.value.customers.filter { customer ->
                    listOf(
                        customer.name,
                        customer.phoneNumber1,
                        customer.phoneNumber2
                    ).any { value ->
                        value.contains(searchQuery, ignoreCase = true)
                    } && customer.residenceCountry == countryFilter
                }.sortedBy { customer -> customer.name }
                _uiState.update { oldState ->
                    oldState.copy(
                        displayedCustomers = filteredCustomers,
                    )
                }
            }
            if (searchQuery.isBlank() && timeFilter == TimeFilter.All) {
                Log.d(TAG, "filterData: searchQuery is blank and timeFilter is All")
                _uiState.update {
                    it.copy(
                        displayedReservations = it.reservations.sortedBy { reservation -> reservation.date + reservation.time },
                    )
                }
            } else if (searchQuery.isBlank()) {
                Log.d(TAG, "filterData: searchQuery is blank")
                val filteredReservations = uiState.value.reservations.filter { reservation ->
                    matchesFilter(
                        timeFilter,
                        reservation.date
                    )
                }.sortedBy { reservation -> reservation.date + reservation.time }
                _uiState.update { oldState ->
                    oldState.copy(
                        displayedReservations = filteredReservations,
                    )
                }
            } else if (searchQuery.startsWith("#")) {
                Log.d(TAG, "filterData: searching for reservation number: $searchQuery")
                val filteredReservations = mutableListOf<Reservation>().apply{
                    uiState.value.reservations.firstOrNull { reservation ->
                        reservation.reservationNumber == (searchQuery.substringAfter("#").toLongOrNull()
                            ?: -1)
                    }?.let {
                       this.add(it)
                    }
                }
                _uiState.update { oldState ->
                    oldState.copy(
                        displayedReservations = filteredReservations,
                        timeFilter = TimeFilter.All
                    )
                }
            } else if (timeFilter == TimeFilter.All) {
                Log.d(TAG, "filterData: timeFilter is All")
                val filteredReservations = uiState.value.reservations.filter { reservation ->
                    listOf(
                        reservation.clientName,
                        reservation.tourismCompany,
                        reservation.travelCompany,
                        reservation.clientPhone,
                        reservation.car
                    ).any { value ->
                        value.contains(searchQuery, ignoreCase = true)
                    }
                }.sortedBy { reservation -> reservation.date + reservation.time }
                _uiState.update { oldState ->
                    oldState.copy(
                        displayedReservations = filteredReservations,
                    )
                }
            } else {
                Log.d(TAG, "filterData: searchQuery and timeFilter are not blank")
                val filteredReservations = uiState.value.reservations.filter { reservation ->
                    listOf(
                        reservation.clientName,
                        reservation.tourismCompany,
                        reservation.travelCompany,
                        reservation.clientPhone,
                        reservation.car
                    ).any { value ->
                        value.contains(searchQuery, ignoreCase = true)
                    } && matchesFilter(timeFilter, reservation.date)
                }.sortedBy {  reservation -> reservation.date + reservation.time }
                _uiState.update { oldState ->
                    oldState.copy(
                        displayedReservations = filteredReservations,
                    )
                }
            }
            Log.d(TAG, "filterData: displayedReservations: ${uiState.value.displayedReservations}, timeFilter: $timeFilter")
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


    private fun deleteCustomer(customerId: String, onShowMessage: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            customerRepo.deleteCustomer(customerId).collect { result ->
                result.onSuccess { data ->
                    onShowMessage(data)
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