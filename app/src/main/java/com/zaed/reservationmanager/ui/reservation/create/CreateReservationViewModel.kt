package com.zaed.reservationmanager.ui.reservation.create

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
import com.zaed.reservationmanager.ui.util.Constants.COUNTRIES_KEY
import com.zaed.reservationmanager.ui.util.Constants.RESERVATION_TYPES_KEY
import com.zaed.reservationmanager.ui.util.InputValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateReservationViewModel(
    private val companyRepository: CompanyRepository,
    private val customerRepository: CustomerRepository,
    private val employeeRepository: EmployeeRepository,
    private val reservationRepository: ReservationRepository,
    private val menuDataStore: MenuDataStore
) : ViewModel() {
    private val TAG = "CreateReservationViewModel"

    private val _uiState = MutableStateFlow(CreateReservationUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchTravelCompanies()
        fetchTourismCompanies()
        fetchReservationTypes()
        fetchCarTypes()
        fetchCountries()
    }

    private fun fetchCountries() {
        viewModelScope.launch {
            menuDataStore.getMenus(COUNTRIES_KEY).collect { data ->
                _uiState.update { oldState ->
                    oldState.copy(
                        countries = data.toList()
                    )
                }
            }
        }
    }

    private fun fetchCarTypes() {
        viewModelScope.launch {
            menuDataStore.getMenus(CAR_TYPES_KEY).collect { data ->
                _uiState.update { oldState ->
                    oldState.copy(
                        carTypes = data.toList()
                    )
                }
            }
        }
    }

    private fun fetchReservationTypes() {
        viewModelScope.launch {
            menuDataStore.getMenus(RESERVATION_TYPES_KEY).collect { data ->
                _uiState.update { oldState ->
                    oldState.copy(
                        reservationTypes = data.toList()
                    )
                }
            }
        }
    }

    private fun fetchTourismCompanies() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            companyRepository.getCompanies(isDriver = false).collect { result ->
                result.onSuccess { companies ->
                    _uiState.update {
                        it.copy(
                            tourismCompanies = companies,
                            isLoading = false
                        )
                    }
                }.onFailure {
                    Log.e(TAG, "fetchTourismCompanies: failed")
                    it.printStackTrace()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                        )
                    }
                }
            }
        }
    }


    private fun fetchTravelCompanies() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            companyRepository.getCompanies(isDriver = true).collect { result ->
                result.onSuccess { companies ->
                    _uiState.update {
                        it.copy(
                            travelCompanies = companies,
                            isLoading = false
                        )
                    }
                }.onFailure {
                    Log.e(TAG, "fetchTravelCompanies: failed")
                    it.printStackTrace()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                        )
                    }
                }
            }
        }
    }

    fun handleAction(action: CreateReservationUiAction) {
        when (action) {
            is CreateReservationUiAction.AddReservation -> addReservation(action.reservation)
            is CreateReservationUiAction.DeleteReservation -> deleteReservation(action.reservationId)
            is CreateReservationUiAction.FetchDrivers -> fetchCompanyEmployees(
                action.companyId,
                driver = true
            )

            is CreateReservationUiAction.FetchEmployees -> fetchCompanyEmployees(
                action.companyId,
                driver = false
            )

            CreateReservationUiAction.SaveReservations -> onSave()
            CreateReservationUiAction.SearchCustomer -> fetchCustomerByNumber()
            is CreateReservationUiAction.UpdateCustomerCountry -> updateCustomerCountry(action.country)
            is CreateReservationUiAction.UpdateCustomerEmail -> updateCustomerEmail(action.email)
            is CreateReservationUiAction.UpdateCustomerName -> updateCustomerName(action.name)
            is CreateReservationUiAction.UpdateCustomerNationality -> updateCustomerNationality(
                action.nationality
            )

            is CreateReservationUiAction.UpdateCustomerPhone -> updateCustomerPhone(action.phone)
            else -> Unit
        }
    }

    private fun updateCustomerPhone(phone: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    customer = it.customer.copy(
                        phoneNumber = phone,
                    ),
                    isNewCustomer = if(phone.isBlank()) null else it.isNewCustomer
                )
            }
        }
    }

    private fun updateCustomerNationality(nationality: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    customer = it.customer.copy(
                        nationality = nationality
                    )
                )
            }
        }
    }

    private fun updateCustomerName(name: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    customer = it.customer.copy(
                        name = name
                    )
                )
            }
        }
    }

    private fun updateCustomerEmail(email: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    customer = it.customer.copy(
                        email = email
                    )
                )
            }
        }
    }

    private fun updateCustomerCountry(country: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    customer = it.customer.copy(
                        residenceCountry = country
                    )
                )
            }
        }
    }

    private fun onSave() {
        viewModelScope.launch {
            if (uiState.value.isNewCustomer == true) {
                _uiState.update {
                    it.copy(isLoading = true)
                }
                with(uiState.value) {
                    if (customer.name.isBlank()) {
                        _uiState.update { it.copy(reservationError = ReservationError.CUSTOMER_NAME_IS_REQUIRED) }
                        return@launch
                    }
                    if (customer.phoneNumber.isBlank()) {
                        _uiState.update { it.copy(reservationError = ReservationError.CUSTOMER_PHONE_IS_REQUIRED) }
                        return@launch
                    }
                    if (!InputValidator.isPhoneNumberValid(customer.phoneNumber)) {
                        _uiState.update { it.copy(reservationError = ReservationError.CUSTOMER_PHONE_IS_INVALID) }
                        return@launch
                    }
                    if (customer.email.isNotBlank() && !InputValidator.isEmailValid(customer.email)) {
                        _uiState.update { it.copy(reservationError = ReservationError.EMAIL_IS_INVALID) }
                        return@launch
                    }
                    _uiState.update { it.copy(reservationError = ReservationError.NONE) }
                    createCustomer()
                }
            } else if (uiState.value.isNewCustomer == false) {
                createReservations()
            }
        }
    }

    private fun createReservations() {
        viewModelScope.launch(Dispatchers.IO) {
            val customer = uiState.value.customer
            val reservations = uiState.value.reservations.map {
                it.copy(
                    clientName = customer.name,
                    clientPhone = customer.phoneNumber,
                    clientCountry = customer.residenceCountry,
                    clientId = customer.id
                )
            }
            reservationRepository.createReservations(reservations).collect { result ->
                result.onSuccess {
                    Log.d(TAG, "createReservations: success")
                    _uiState.update {
                        it.copy(
                            isFinished = true
                        )
                    }
                }.onFailure {
                    Log.e(TAG, "createReservations: failed to create reservations ${it.message}")
                    it.printStackTrace()
                }
            }
        }
    }

    private fun addReservation(reservation: Reservation) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    reservations = it.reservations + reservation
                )
            }
        }
    }

    private fun deleteReservation(reservationId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    reservations = it.reservations.filter { reservation -> reservation.id != reservationId }
                )
            }
        }
    }

    private fun createCustomer() {
        viewModelScope.launch {
            customerRepository.createCustomer(
                uiState.value.customer
            ).collect { result ->
                result.onSuccess { data ->
                    Log.d(TAG, "createCustomer: success")
                    _uiState.update {
                        it.copy(
                            customer = it.customer.copy(
                                id = data
                            )
                        )
                    }
                    createReservations()
                }.onFailure {
                    Log.d(TAG, "createNewCustomer: failed to create customer${it.message}")
                    it.printStackTrace()
                }
            }
        }
    }

    private fun fetchCompanyEmployees(companyId: String, driver: Boolean = false) {
        viewModelScope.launch {
            employeeRepository.getEmployeesByCompany(companyId).collect { result ->
                result.onSuccess { data ->
                    Log.d(TAG, "fetchCompanyEmployees: success: $data")
                    if (driver) {
                        _uiState.update { oldState ->
                            oldState.copy(
                                drivers = data
                            )
                        }
                    } else {
                        _uiState.update { oldState ->
                            oldState.copy(
                                employees = data
                            )
                        }
                    }
                }.onFailure {
                    Log.e(TAG, "fetchCompanyEmployees: failed")
                    it.printStackTrace()
                }
            }
        }
    }

    private fun fetchCustomerByNumber() {
        viewModelScope.launch {
            customerRepository.getCustomerByNumber(uiState.value.customer.phoneNumber)
                .onSuccess { customer ->
                    Log.d(TAG, "fetchCustomerByNumber: $customer")
                    _uiState.update { oldState ->
                        oldState.copy(
                            isNewCustomer = customer.id.isEmpty(),
                            customer = oldState.customer.copy(
                                id = customer.id,
                                name = customer.name,
                                email = customer.email,
                                residenceCountry = customer.residenceCountry,
                                nationality = customer.nationality,
                                createdAtEpochSeconds = customer.createdAtEpochSeconds
                            )
                        )
                    }
                }.onFailure { error ->
                    Log.d(TAG, "fetchCustomerByNumber: ${error.message}")
                }
        }
    }
}

