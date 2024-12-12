package com.zaed.reservationmanager.ui.reservation.create

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.data.model.Ride
import com.zaed.reservationmanager.data.repository.CompanyRepository
import com.zaed.reservationmanager.data.repository.CustomerRepository
import com.zaed.reservationmanager.data.repository.EmployeeRepository
import com.zaed.reservationmanager.data.repository.ReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateReservationViewModel(
    private val companyRepository: CompanyRepository,
    private val customerRepository: CustomerRepository,
    private val employeeRepository: EmployeeRepository,
    private val reservationRepository: ReservationRepository
) : ViewModel() {
    val TAG = "CreateReservationViewModel"

    private val _state = MutableStateFlow(CreateReservationState())
    val state = _state.asStateFlow()

    init {
        fetchTravelCompanies()
        fetchTourismCompanies()
    }

    private fun fetchTourismCompanies() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            companyRepository.getCompanies(isDriver = false).collect { result ->
                result.onSuccess { companies ->
                    _state.update {
                        it.copy(
                            tourismCompanies = companies,
                            loading = false
                        )
                    }
                }.onFailure {
                    _state.update {
                        it.copy(
                            loading = false,
                            userMessage = "Failed to fetch companies"
                        )
                    }
                }
            }
        }
    }


    private fun fetchTravelCompanies() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            companyRepository.getCompanies(isDriver = true).collect { result ->
                result.onSuccess { companies ->
                    _state.update {
                        it.copy(
                            travelCompanies = companies,
                            loading = false
                        )
                    }
                }.onFailure {
                    _state.update {
                        it.copy(
                            loading = false,
                            userMessage = "Failed to fetch companies"
                        )
                    }
                }
            }
        }
    }

    fun handleAction(reservationUiAction: ReservationUiAction) {
        when (reservationUiAction) {
            ReservationUiAction.AddReservation -> TODO()
            ReservationUiAction.Cancel -> cancelReservation()
            is ReservationUiAction.UpdateCollectionPrice -> updateCollectionPrice(
                reservationUiAction.price
            )

            is ReservationUiAction.UpdateCustomerCountry -> updateCustomerCountry(
                reservationUiAction.country
            )

            is ReservationUiAction.UpdateCustomerNumber -> updateCustomerNumber(reservationUiAction.number)
            is ReservationUiAction.UpdateDriver -> updateDriver(reservationUiAction.driver)
            is ReservationUiAction.UpdateEndLocation -> updateEndLocation(reservationUiAction.location)
            is ReservationUiAction.UpdateMovementPrice -> updateMovementPrice(reservationUiAction.price)
            is ReservationUiAction.UpdateNote -> updateNote(reservationUiAction.note)
            is ReservationUiAction.UpdateReservationCar -> updateReservationCar(reservationUiAction.car)
            is ReservationUiAction.UpdateReservationDate -> updateReservationDate(
                reservationUiAction.date
            )

            is ReservationUiAction.UpdateReservationTime -> updateReservationTime(
                reservationUiAction.time ?: 0L
            )

            is ReservationUiAction.UpdateReservationType -> updateReservationType(
                reservationUiAction.type
            )

            is ReservationUiAction.UpdateSelectedTourismCompany -> updateSelectedTourismCompany(
                reservationUiAction.company
            )

            is ReservationUiAction.UpdateSelectedTravelCompany -> updateSelectedTravelCompany(
                reservationUiAction.company
            )

            is ReservationUiAction.UpdateStartLocation -> updateStartLocation(reservationUiAction.location)
            is ReservationUiAction.UpdateTourismEmployee -> updateTourismEmployee(
                reservationUiAction.employee
            )

            is ReservationUiAction.UpdateCustomerName -> updateCustomerName(reservationUiAction.name)
            ReservationUiAction.SearchClient -> fetchCustomerByNumber(_state.value.reservation.clientPhone)
            is ReservationUiAction.UpdateTravelNumber -> updateTravelNumber(reservationUiAction.number)
            ReservationUiAction.AddMovement -> addMovements()
        }
    }

    private fun cancelReservation() {
        _state.update {
            CreateReservationState()
        }
    }

    private fun addMovements() {
        viewModelScope.launch {
            with(_state.value) {
                if (newRide.date == 0L) {
                    _state.update {
                        it.copy(
                            errorMessage = ReservationError.DATE_IS_REQUIRED,
                        )
                    }
                } else if (time == 0L) {
                    _state.update {
                        it.copy(
                            errorMessage = ReservationError.TIME_IS_REQUIRED,
                        )
                    }

                } else if (newRide.type.isBlank()) {
                    _state.update {
                        it.copy(
                            errorMessage = ReservationError.TYPE_IS_REQUIRED,
                        )
                    }
                } else if (newRide.car.isBlank()) {
                    _state.update {
                        it.copy(
                            errorMessage = ReservationError.CAR_IS_REQUIRED,
                        )
                    }
                } else if (newRide.startLocation.isBlank()) {
                    _state.update {
                        it.copy(
                            errorMessage = ReservationError.START_LOCATION_IS_REQUIRED,
                        )
                    }
                } else if (newRide.endLocation.isBlank()) {
                    _state.update {
                        it.copy(
                            errorMessage = ReservationError.END_LOCATION_IS_REQUIRED,
                        )
                    }
                } else if (newRide.buyingPrice == 0.0) {
                    _state.update {
                        it.copy(
                            errorMessage = ReservationError.BUYING_PRICE_IS_REQUIRED,
                        )
                    }
                } else if (newRide.collectedPrice == 0.0) {
                    _state.update {
                        it.copy(
                            errorMessage = ReservationError.COLLECTION_PRICE_IS_REQUIRED,
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            errorMessage = ReservationError.NONE
                        )
                    }
                    if (
                        customer.name == reservation.clientName
                        && customer.phoneNumber == reservation.clientPhone
                        && customer.residenceCountry == reservation.clientCountry

                    ) {
                        if (state.value.reservation.id.isBlank()) {
                            createReservation()
                        } else {
                            createRide()
                        }
                    } else {
                        if(state.value.reservation.id.isBlank()) {
                            createNewCustomer()
                        }else{
                            createRide()
                        }
                    }

                }
            }
        }
    }

    private fun createNewCustomer() {
        viewModelScope.launch {
            customerRepository.createCustomer(
                Customer(
                    name = _state.value.reservation.clientName,
                    phoneNumber = _state.value.reservation.clientPhone,
                    residenceCountry = _state.value.reservation.clientCountry,
                )
            ).collect{result->
                result.onSuccess { data->
                    _state.update {
                        it.copy(reservation = it.reservation.copy(clientId =data))
                    }
                    createReservation()
                }.onFailure {
                    _state.update {
                        it.copy(
                            userMessage = "Failed to create customer"
                        )
                    }
                }
            }
        }
    }

    private fun createReservation() {
        viewModelScope.launch {
            reservationRepository.createReservation(
                _state.value.reservation.copy(
                    clientId = state.value.customer.id
                ),
            ).collect { result ->
                result.onSuccess { data ->
                    _state.update {
                        it.copy(
                            reservation = it.reservation.copy(id = data),
                        )
                    }
                    createRide()
                }.onFailure {
                    _state.update {
                        it.copy(
                            userMessage = "Failed to create reservation"
                        )
                    }
                }
            }
        }
    }

    private fun createRide() {
        viewModelScope.launch {
            reservationRepository.createRide(
                _state.value.newRide.copy(reservationId = _state.value.reservation.id)
            ).collect { result ->
                result.onSuccess { data ->
                    _state.update {
                        it.copy(
                            newRide = Ride().copy(reservationId = _state.value.reservation.id),
                            successStatus = true
                        )
                    }
                    fetchRideByReservationId(_state.value.reservation.id)
                }.onFailure {
                    _state.update {
                        it.copy(
                            userMessage = "Failed to create ride"
                        )
                    }
                }
            }
        }
    }

    private fun fetchRideByReservationId(reservationId: String) {
        viewModelScope.launch {
            reservationRepository.getRidesByReservationId(reservationId).collect { result ->
                result.onSuccess { data ->
                    _state.update {
                        it.copy(
                            rides = data
                        )
                    }
                }.onFailure {
                    _state.update {
                        it.copy(
                            userMessage = "Failed to fetch rides"
                        )
                    }
                }
            }
        }
    }


    private fun updateTravelNumber(number: String) {
        _state.update {
            it.copy(
                reservation = it.reservation.copy(
                    flightNumber = number
                )
            )
        }
    }

    private fun updateCustomerName(name: String) {
        _state.update {
            it.copy(
                reservation = it.reservation.copy(
                    clientName = name
                )
            )
        }
    }

    private fun updateTourismEmployee(employee: Employee) {
        _state.update {
            it.copy(
                reservation = it.reservation.copy(
                    tourismEmployee = employee.name,
                    tourismEmployeeId = employee.id,
                    tourismEmployeePhone = employee.phoneNumber1
                )
            )
        }
    }

    private fun updateStartLocation(location: String) {
        _state.update {
            it.copy(
                newRide = it.newRide.copy(
                    startLocation = location
                )
            )
        }
    }

    private fun updateSelectedTravelCompany(company: Company) {
        _state.update { oldState ->
            oldState.copy(
                newRide = oldState.newRide.copy(
                    travelCompany = company.name,
                    travelCompanyId = company.id
                )
            )
        }
        //todo make it by id
        fetchCompanyEmployees(company.name, true)
    }

    private fun fetchCompanyEmployees(company: String, driver: Boolean = false) {
        viewModelScope.launch {
            employeeRepository.getEmployeesByCompany(company).collect { result ->
                result.onSuccess { data ->
                    if (driver) {
                        _state.update { oldState ->
                            oldState.copy(
                                drivers = data
                            )
                        }
                    } else {
                        _state.update { oldState ->
                            oldState.copy(
                                employees = data
                            )
                        }
                    }
                }.onFailure {
                    _state.update { oldState ->
                        oldState.copy(
                            userMessage = "Failed to fetch employees"
                        )
                    }
                }
            }
        }
    }

    private fun updateSelectedTourismCompany(company: Company) {
        _state.update { oldState ->
            oldState.copy(
                reservation = oldState.reservation.copy(
                    tourismCompany = company.name,
                    tourismCompanyId = company.id,
                    tourismCompanyPhone = company.phoneNumber
                )
            )
        }
        fetchCompanyEmployees(company.name)
    }

    private fun updateReservationType(type: String) {
        _state.update {
            it.copy(
                newRide = it.newRide.copy(
                    type = type
                )
            )
        }
    }

    private fun updateReservationTime(time: Long) {
        _state.update {
            it.copy(
                newRide = it.newRide.copy(
                    date = it.newRide.date + time,
                ),
                time = time
            )
        }
    }

    private fun updateReservationDate(date: Long?) {
        _state.update {
            it.copy(
                newRide = it.newRide.copy(
                    date = date ?: 0L
                )
            )
        }
    }

    private fun updateReservationCar(car: String) {
        _state.update {
            it.copy(
                newRide = it.newRide.copy(
                    car = car
                )
            )
        }
    }

    private fun updateNote(note: String) {
        _state.update {
            it.copy(
                newRide = it.newRide.copy(
                    note = note
                )
            )
        }
    }

    private fun updateMovementPrice(price: String) {
        _state.update {
            it.copy(
                newRide = it.newRide.copy(
                    buyingPrice = price.toDouble()
                )
            )
        }
    }

    private fun updateEndLocation(location: String) {
        _state.update {
            it.copy(
                newRide = it.newRide.copy(
                    endLocation = location
                )
            )
        }
    }

    private fun updateDriver(driver: Employee) {
        _state.update {
            it.copy(
                newRide = it.newRide.copy(
                    driver = driver.name,
                    driverId = driver.id
                )
            )
        }
    }

    private fun updateCustomerNumber(number: String) {
        _state.update { oldState ->
            oldState.copy(
                reservation = oldState.reservation.copy(
                    clientPhone = number
                )
            )
        }

    }

    private fun fetchCustomerByNumber(number: String) {
        viewModelScope.launch {
            customerRepository.getCustomerByNumber(number)
                .onSuccess { customer ->
                    Log.d(TAG, "fetchCustomerByNumber: $customer")
                    _state.update {
                        it.copy(
                            reservation = it.reservation.copy(
                                clientName = customer.name,
                                clientPhone = customer.phoneNumber,
                                clientCountry = customer.residenceCountry
                            ),
                            customer = customer
                        )
                    }
                }.onFailure {
                    _state.update {
                        it.copy(
                            userMessage = "User not found"
                        )
                    }
                    Log.d(TAG, "fetchCustomerByNumber: ${it.message}")
                }

        }
    }

    private fun updateCustomerCountry(country: String) {
        _state.update { oldState ->
            oldState.copy(
                reservation = oldState.reservation.copy(
                    clientCountry = country
                ),
            )
        }
    }


    private fun updateCollectionPrice(price: String) {
        _state.update { oldState ->
            oldState.copy(
                newRide = oldState.newRide.copy(
                    collectedPrice = price.toDouble()
                )
            )
        }
    }
}

