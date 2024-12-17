package com.zaed.reservationmanager.ui.reservation.create

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.data.model.ReservationModel
import com.zaed.reservationmanager.data.repository.CompanyRepository
import com.zaed.reservationmanager.data.repository.CustomerRepository
import com.zaed.reservationmanager.data.repository.EmployeeRepository
import com.zaed.reservationmanager.data.repository.ReservationRepository
import com.zaed.reservationmanager.ui.dropdownmenu.MenuDataStore
import com.zaed.reservationmanager.ui.util.Constants.CAR_TYPES_KEY
import com.zaed.reservationmanager.ui.util.Constants.COUNTRIES_KEY
import com.zaed.reservationmanager.ui.util.Constants.RESERVATION_TYPES_KEY
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
    private var isEditMode = false
    private val TAG = "CreateReservationViewModel"

    private val _state = MutableStateFlow(CreateReservationState())
    val state = _state.asStateFlow()

    init {
        fetchTravelCompanies()
        fetchTourismCompanies()
        fetchTransactionTypes()
        fetchCarTypes()
        fetchCountries()
    }

    private fun fetchCountries() {
        viewModelScope.launch {
            menuDataStore.getMenus(COUNTRIES_KEY).collect { data ->
                _state.update { oldState ->
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
                _state.update { oldState ->
                    oldState.copy(
                        carTypes = data.toList()
                    )
                }
            }
        }
    }

    private fun fetchTransactionTypes() {
        viewModelScope.launch {
            menuDataStore.getMenus(RESERVATION_TYPES_KEY).collect {data->
                _state.update { oldState ->
                    oldState.copy(
                        transactionTypes = data.toList()
                    )
                }
            }
        }
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
            is ReservationUiAction.DeleteRide -> {
                if(isEditMode) {
                    deleteRemoteRide(reservationUiAction.rideId)
                } else {
                    deleteLocalRide(reservationUiAction.rideId)
                }
            }
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
            ReservationUiAction.SaveReservation -> saveReservationData()
            ReservationUiAction.ValidateReservationData -> validateReservationData()
            is ReservationUiAction.UpdateBuyingPrice -> updateBuyingPrice(reservationUiAction.price)
            is ReservationUiAction.UpdateSellingPrice -> updateSellingPrice(reservationUiAction.price)
            is ReservationUiAction.EditRide -> editSelectedRide(reservationUiAction.reservationModel)
        }
    }

    private fun deleteLocalRide(rideId: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    reservationModels = it.reservationModels.filter { ride -> ride.id != rideId }
                )
            }
        }
    }

    private fun deleteRemoteRide(rideId: String) {
        viewModelScope.launch (Dispatchers.IO){
            reservationRepository.deleteRide(rideId).collect{ result ->
                result.onSuccess {
                    deleteLocalRide(rideId)
                }.onFailure {
                    Log.e(TAG, "deleteRemoteRide: failed to delete ride")
                    it.printStackTrace()
                }
            }
        }
    }

    private fun editSelectedRide(reservationModel: ReservationModel) {
        _state.update {
            it.copy(
                newReservationModel = reservationModel
            )
        }
    }

    private fun updateSellingPrice(price: String) {
        _state.update {
            it.copy(
                newReservationModel = it.newReservationModel.copy(sellingPrice = price.toDouble()),
                rideError = ReservationError.NONE
            )
        }
    }

    private fun updateBuyingPrice(price: String) {
        _state.update {
            it.copy(
                newReservationModel = it.newReservationModel.copy(buyingPrice = price.toDouble()),
                rideError = ReservationError.NONE
            )
        }
    }

    private fun validateReservationData(): Boolean {
        with(_state.value) {
            if (reservation.clientPhone.isBlank()) {
                _state.update {
                    it.copy(
                        reservationError = ReservationError.CUSTOMER_PHONE_IS_REQUIRED,
                    )
                }
                return false
            } else if (reservation.clientPhone.length < 10) {
                _state.update {
                    it.copy(
                        reservationError = ReservationError.CUSTOMER_PHONE_IS_INVALID,
                    )
                }
                return false
            } else if (reservation.clientName.isBlank()) {
                _state.update {
                    it.copy(
                        reservationError = ReservationError.CUSTOMER_NAME_IS_REQUIRED,
                    )
                }
                return false
            } else if (reservation.tourismCompany.isBlank()) {
                _state.update {
                    it.copy(
                        reservationError = ReservationError.TOURISM_COMPANY_IS_REQUIRED,
                    )
                }
                return false
            } else if (reservation.tourismEmployee.isBlank()) {
                _state.update {
                    it.copy(
                        reservationError = ReservationError.TOURISM_EMPLOYEE_IS_REQUIRED,
                    )
                }
                return false
            } else {
                return true
            }
        }
    }

    private fun validateRideData(): Boolean {
        with(_state.value) {
            if (newReservationModel.date == 0L) {
                _state.update {
                    it.copy(
                        rideError = ReservationError.DATE_IS_REQUIRED,
                    )
                }
                Log.e(TAG, "validateRideData: invalid date", )
                return false
            } else if (time == 0L && !isEditMode) {
                _state.update {
                    it.copy(
                        rideError = ReservationError.TIME_IS_REQUIRED,
                    )
                }
                Log.e(TAG, "validateRideData: invalid time", )
                return false
            } else if (newReservationModel.type.isBlank()) {
                _state.update {
                    it.copy(
                        rideError = ReservationError.TYPE_IS_REQUIRED,
                    )
                }
                Log.e(TAG, "validateRideData: invalid type", )
                return false
            } else if (newReservationModel.car.isBlank()) {
                _state.update {
                    it.copy(
                        rideError = ReservationError.CAR_IS_REQUIRED,
                    )
                }
                Log.e(TAG, "validateRideData: invalid car", )
                return false
            } else if (newReservationModel.startLocation.isBlank()) {
                _state.update {
                    it.copy(
                        rideError = ReservationError.START_LOCATION_IS_REQUIRED,
                    )
                }
                Log.e(TAG, "validateRideData: invalid start location", )
                return false
            } else if (newReservationModel.endLocation.isBlank()) {
                _state.update {
                    it.copy(
                        rideError = ReservationError.END_LOCATION_IS_REQUIRED,
                    )
                }
                Log.e(TAG, "validateRideData: invalid end location", )
                return false
            } else if (newReservationModel.sellingPrice == 0.0) {
                _state.update {
                    it.copy(
                        rideError = ReservationError.SELLING_PRICE_IS_REQUIRED,
                    )
                }
                Log.e(TAG, "validateRideData: invalid selling price", )
                return false
            } else {
                _state.update {
                    it.copy(
                        rideError = ReservationError.NONE
                    )
                }
                return true
            }
        }
    }

    private fun isOldCustomer(): Boolean {
        return with(_state.value) {
            customer.name == reservation.clientName
                    && customer.phoneNumber == reservation.clientPhone
                    && customer.residenceCountry == reservation.clientCountry
        }

    }

    private fun saveReservationData() {
        if (!validateReservationData()) return
        if(!isEditMode) {
            if (isOldCustomer()) {
                createReservation()
            } else {
                createNewCustomer()
            }
        }else{
            if(isOldCustomer()) {
                updateReservation()
            }else{
                createNewCustomer()
            }

        }
    }

    private fun updateReservation() {
        viewModelScope.launch {
            reservationRepository.updateReservation(state.value.reservation).collect { result ->
                result.onSuccess {
                    if(state.value.reservationModels.isEmpty()){
                        _state.update {
                            it.copy(
                                successStatus = true
                            )
                        }
                    }else {
                        state.value.reservationModels.forEach{ ride ->
                            if(ride.id.isBlank()){
                                createRide(
                                    ride.copy(
                                        reservationId = state.value.reservation.id,
                                        reservationNumber = state.value.reservation.reservationNumber,
                                        tourismCompanyId = state.value.reservation.tourismCompanyId,
                                        customerId = state.value.customer.id,
                                        clientName = state.value.reservation.clientName
                                    )
                                )
                            } else {
                                updateRide(ride)
                            }
                        }
                    }
                }.onFailure {
                    Log.d(TAG, "updateReservation: failed to update")
                    _state.update {
                        it.copy(
                            userMessage = "Failed to update reservation"
                        )
                    }
                }
            }
        }
    }

    private fun updateRide(reservationModel: ReservationModel) {
        viewModelScope.launch {
            reservationRepository.updateRide(reservationModel).collect { result ->
                result.onSuccess {
                    Log.d(TAG, "updateRide: ride updated")
                    _state.update {
                        it.copy(
                            successStatus = true
                        )
                    }
                }.onFailure {
                    Log.d(TAG, "updateRide: failed to update ride")
                }
            }
        }
    }

    private fun cancelReservation() {
        _state.update {
            CreateReservationState()
        }

    }

    private fun addMovements() {
        Log.d(TAG, "addMovements: ")
        if (!validateRideData()) return
        val ride = state.value.newReservationModel
        _state.update {
            it.copy(
                reservationError = ReservationError.NONE,
                reservationModels = if(!isEditMode || ride.id.isBlank()) it.reservationModels + ride else it.reservationModels.map { r -> if(r.id == ride.id) ride else r },
                successStatus = true,
                newReservationModel = ReservationModel()
            )
        }
    }

    private fun createNewCustomer() {
        viewModelScope.launch {
            Log.d(TAG, "createNewCustomer: ${_state.value.reservation}")
            customerRepository.createCustomer(
                Customer(
                    id = _state.value.reservation.clientId,
                    name = _state.value.reservation.clientName,
                    phoneNumber = _state.value.reservation.clientPhone,
                    residenceCountry = _state.value.reservation.clientCountry,
                )
            ).collect { result ->
                result.onSuccess { data ->
                    _state.update {
                        it.copy(reservation = it.reservation.copy(clientId = data))
                    }
                    if(!isEditMode) {
                        createReservation()
                    }else{
                        updateReservation()
                    }
                }.onFailure {
                    Log.d(TAG, "createNewCustomer: failed to create customer${it.message}")
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
                            reservation = it.reservation.copy(id = data.first),
                        )
                    }
                    if(state.value.reservationModels.isEmpty()){
                        _state.update {
                            it.copy(
                                successStatus = true
                            )
                        }
                    }else {
                        state.value.reservationModels.forEach { ride ->
                            createRide(
                                ride.copy(
                                    reservationId = data.first,
                                    reservationNumber = data.second,
                                    tourismCompanyId = state.value.reservation.tourismCompanyId,
                                    customerId = state.value.customer.id,
                                    clientName = state.value.reservation.clientName
                                )
                            )
                        }
                    }
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

    private fun createRide(reservationModel: ReservationModel) {
        viewModelScope.launch {
            reservationRepository.createRide(
                reservationModel
            ).collect { result ->
                result.onSuccess {
                    _state.update {
                        it.copy(
                            successStatus = true
                        )
                    }
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

    private fun updateTravelNumber(number: String) {
        _state.update {
            it.copy(
                reservation = it.reservation.copy(
                    flightNumber = number
                ),
                reservationError = ReservationError.NONE
            )
        }
    }

    private fun updateCustomerName(name: String) {
        _state.update {
            it.copy(
                reservation = it.reservation.copy(
                    clientName = name
                ),
                reservationError = ReservationError.NONE
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
                ),
                reservationError = ReservationError.NONE
            )
        }
    }

    private fun updateStartLocation(location: String) {
        _state.update {
            it.copy(
                newReservationModel = it.newReservationModel.copy(
                    startLocation = location
                ),
                rideError = ReservationError.NONE
            )
        }
    }

    private fun updateSelectedTravelCompany(company: Company) {
        _state.update { oldState ->
            oldState.copy(
                newReservationModel = oldState.newReservationModel.copy(
                    travelCompany = company.name,
                    travelCompanyId = company.id,
                    travelCompanyPhone = company.phoneNumber
                ),
                rideError = ReservationError.NONE
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
                ),
                reservationError = ReservationError.NONE
            )
        }
        fetchCompanyEmployees(company.name)
    }

    private fun updateReservationType(type: String) {
        _state.update {
            it.copy(
                newReservationModel = it.newReservationModel.copy(
                    type = type
                ),
                rideError = ReservationError.NONE
            )
        }
    }

    private fun updateReservationTime(time: Long) {
        Log.d(TAG, "updateReservationTime: $time")
        _state.update {
            it.copy(
                newReservationModel = it.newReservationModel.copy(
                    date = it.newReservationModel.date + time,
                ),
                time = time,
                rideError = ReservationError.NONE
            )
        }
    }

    private fun updateReservationDate(date: Long?) {
        Log.d(TAG, "updateReservationDate: $date")
        _state.update {
            it.copy(
                newReservationModel = it.newReservationModel.copy(
                    date = date ?: 0L
                ),
                rideError = ReservationError.NONE
            )
        }
    }

    private fun updateReservationCar(car: String) {
        _state.update {
            it.copy(
                newReservationModel = it.newReservationModel.copy(
                    car = car
                ),
                rideError = ReservationError.NONE
            )
        }
    }

    private fun updateNote(note: String) {
        _state.update {
            it.copy(
                newReservationModel = it.newReservationModel.copy(
                    note = note
                ),
                rideError = ReservationError.NONE
            )
        }
    }

    private fun updateEndLocation(location: String) {
        _state.update {
            it.copy(
                newReservationModel = it.newReservationModel.copy(
                    endLocation = location
                ),
                rideError = ReservationError.NONE
            )
        }
    }

    private fun updateDriver(driver: Employee) {
        _state.update {
            it.copy(
                newReservationModel = it.newReservationModel.copy(
                    driver = driver.name,
                    driverId = driver.id
                ),
                rideError = ReservationError.NONE
            )
        }
    }

    private fun updateCustomerNumber(number: String) {
        _state.update { oldState ->
            oldState.copy(
                reservation = oldState.reservation.copy(
                    clientPhone = number
                ),
                reservationError = ReservationError.NONE
            )
        }

    }

    private fun fetchCustomerByNumber(number: String) {
        Log.d(TAG, "fetchCustomerByNumber: $number")
        viewModelScope.launch {
            customerRepository.getCustomerByNumber(number)
                .onSuccess { customer ->
                    Log.d(TAG, "fetchCustomerByNumber: $customer")
                    _state.update {
                        it.copy(
                            reservation = it.reservation.copy(
                                clientId =  customer.id,
                                clientName = customer.name,
                                clientPhone = customer.phoneNumber,
                                clientCountry = customer.residenceCountry
                            ),
                            customer = customer
                        )
                    }
                }.onFailure { error ->
                    _state.update {
                        it.copy(
                            userMessage = "User not found"
                        )
                    }
                    Log.d(TAG, "fetchCustomerByNumber: ${error.message}")
                }

        }
    }

    private fun updateCustomerCountry(country: String) {
        _state.update { oldState ->
            oldState.copy(
                reservation = oldState.reservation.copy(
                    clientCountry = country
                ),
                reservationError = ReservationError.NONE
            )
        }
    }


    private fun updateCollectionPrice(price: String) {
        _state.update { oldState ->
            oldState.copy(
                newReservationModel = oldState.newReservationModel.copy(
                    collectedAmount = price.toDouble()
                ),
                rideError = ReservationError.NONE
            )
        }
    }

    fun init(reservation: Reservation, initialCustomer: Customer) {
        if(reservation.id.isNotBlank()){
            isEditMode = true
            _state.update {
                it.copy(
                    reservation = reservation,
                    customer = Customer(
                        id = reservation.clientId,
                        name = reservation.clientName,
                        phoneNumber = reservation.clientPhone,
                        residenceCountry = reservation.clientCountry
                    )
                )
            }
            fetchRides(reservation.id)
        } else {
            _state.update {
                it.copy(
                    reservation = reservation.copy(
                        clientName = initialCustomer.name,
                        clientId = initialCustomer.id,
                        clientCountry = initialCustomer.residenceCountry,
                        clientPhone = initialCustomer.phoneNumber
                    ),
                    customer = initialCustomer
                )
            }
        }
    }

    private fun fetchRides(reservationId: String) {
        viewModelScope.launch (Dispatchers.IO){
            reservationRepository.getRidesByReservationId(reservationId).collect { result ->
                result.onSuccess { rides ->
                    _state.update {
                        it.copy(
                            reservationModels = rides
                        )
                    }
                }.onFailure {
                    Log.d(TAG, "fetchRides: ${it.message}")
                }
            }
        }
    }

    fun resetSuccessStatus() {
        _state.update {
            it.copy(
                successStatus = false
            )
        }
    }
}

