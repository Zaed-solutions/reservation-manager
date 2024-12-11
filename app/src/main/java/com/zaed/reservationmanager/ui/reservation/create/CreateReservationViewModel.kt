package com.zaed.reservationmanager.ui.reservation.create

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
            companyRepository.getCompaniesNames(isDriver = false).collect { result ->
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
        }    }


    private fun fetchTravelCompanies() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            companyRepository.getCompaniesNames(isDriver = true).collect { result ->
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
            ReservationUiAction.Cancel -> TODO()
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
                reservationUiAction.time
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
            ReservationUiAction.SearchClient -> fetchCustomerByNumber(_state.value.customer.phoneNumber)
            is ReservationUiAction.UpdateTravelNumber -> updateTravelNumber(reservationUiAction.number)
            ReservationUiAction.AddMovement -> TODO()
            ReservationUiAction.GetMovementsForUser -> getMovementForCustomerAndCompany(
                _state.value.customer.phoneNumber,
                _state.value.selectedTravelCompany
            )
        }
    }

    private fun getMovementForCustomerAndCompany(
        phoneNumber: String,
        selectedTravelCompany: String
    ) {

    }

    private fun updateTravelNumber(number: String) {
        _state.update {
            it.copy(
                reservation = it.reservation.copy(
                    travelNumber = number
                )
            )
        }
    }

    private fun updateCustomerName(name: String) {
        _state.update {
            it.copy(
                customer = it.customer.copy(
                    name = name
                )
            )
        }
    }

    private fun updateTourismEmployee(employee: String) {
        _state.update {
            it.copy(
                reservation = it.reservation.copy(
                    tourismEmployee = employee
                )
            )
        }
    }

    private fun updateStartLocation(location: String) {
        _state.update {
            it.copy(
                newMovement = it.newMovement.copy(
                    startLocation = location
                )
            )
        }
    }

    private fun updateSelectedTravelCompany(company: String) {
        _state.update {
            it.copy(
                selectedTravelCompany = company
            )
        }
        fetchCompanyEmployees(company,true)
    }

    private fun fetchCompanyEmployees(company: String,driver: Boolean = false) {
        viewModelScope.launch {
            employeeRepository.getEmployeesByCompany(company).collect { result ->
                result.onSuccess { data->
                    if (driver){
                    _state.update { oldState ->
                        oldState.copy(
                            drivers = data.map { it.name }
                        )
                    }}else{
                        _state.update { oldState ->
                            oldState.copy(
                                employees = data.map { it.name }
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

    private fun updateSelectedTourismCompany(company: String) {
        _state.update {
            it.copy(
                selectedTourismCompany = company
            )
        }
        fetchCompanyEmployees(company)
    }

    private fun updateReservationType(type: String) {
        _state.update {
            it.copy(
                newMovement = it.newMovement.copy(
                    type = type
                )
            )
        }
    }

    private fun updateReservationTime(time: Long?) {
        _state.update {
            it.copy(
                newMovement = it.newMovement.copy(
                    time = time ?: 0L
                )
            )
        }
    }

    private fun updateReservationDate(date: Long?) {
        _state.update {
            it.copy(
                newMovement = it.newMovement.copy(
                    date = date ?: 0L
                )
            )
        }
    }

    private fun updateReservationCar(car: String) {
        _state.update {
            it.copy(
                newMovement = it.newMovement.copy(
                    car = car
                )
            )
        }
    }

    private fun updateNote(note: String) {
        _state.update {
            it.copy(
                newMovement = it.newMovement.copy(
                    note = note
                )
            )
        }
    }
    private fun updateMovementPrice(price: String) {
        _state.update {
            it.copy(
                newMovement = it.newMovement.copy(
                    buyingPrice = price.toDouble()
                )
            )
        }
    }

    private fun updateEndLocation(location: String) {
        _state.update {
            it.copy(
                newMovement = it.newMovement.copy(
                    endLocation = location
                )
            )
        }
    }

    private fun updateDriver(driver: String) {
        _state.update {
            it.copy(
                newMovement = it.newMovement.copy(
                    driver = driver
                )
            )
        }
    }

    private fun updateCustomerNumber(number: String) {
        _state.update { oldState ->
            oldState.copy(
                customer = oldState.customer.copy(
                    phoneNumber = number
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
                customer = oldState.customer.copy(
                    residenceCountry = country
                )
            )
        }
    }


    private fun updateCollectionPrice(price: String) {
        _state.update { oldState ->
            oldState.copy(
                newMovement = oldState.newMovement.copy(
                    collectionPrice = price.toDouble()
                )
            )
        }
    }
}

