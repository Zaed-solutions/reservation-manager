package com.zaed.reservationmanager.ui.employee.add

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.data.model.EmployeeType
import com.zaed.reservationmanager.data.repository.CompanyRepository
import com.zaed.reservationmanager.data.repository.EmployeeRepository
import com.zaed.reservationmanager.ui.util.InputValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class AddEmployeeViewModel(
    private val companyRepo: CompanyRepository,
    private val employeeRepo: EmployeeRepository,
) : ViewModel() {
    private val TAG = "AddEmployeeViewModel"
    private val _uiState = MutableStateFlow(AddEmployeeUiState())
    val uiState = _uiState.asStateFlow()
    fun init(initialEmployee: Employee, isDriver: Boolean) {
        viewModelScope.launch {
            if (initialEmployee.id.isNotBlank()) {
                _uiState.update { it.copy(employee = initialEmployee, isNew = false) }
            }
            if (isDriver) {
                _uiState.update { oldState ->
                    oldState.copy(
                        isDriver = true,
                        employee = oldState.employee.copy(position = EmployeeType.DRIVER.name)
                    )
                }
            }
            fetchCompanies()
        }
    }

    private fun fetchCompanies() {
        viewModelScope.launch(Dispatchers.IO) {
            companyRepo.getCompanies(uiState.value.isDriver).collect { result ->
                result.onSuccess { data ->
                    _uiState.update { oldState ->
                        oldState.copy(companies = data)
                    }
                }.onFailure { error ->
                    Log.e(TAG, error.message.toString())
                    error.printStackTrace()
                }
            }
        }
    }

    fun handleAction(action: AddEmployeeUiAction) {
        when (action) {
            is AddEmployeeUiAction.OnCompanyChanged -> updateCompany(action.company)
            is AddEmployeeUiAction.OnNameChanged -> updateName(action.name)
            is AddEmployeeUiAction.OnPhoneNumber1Changed -> updatePhoneNumber1(action.phoneNumber)
            is AddEmployeeUiAction.OnPhoneNumber2Changed -> updatePhoneNumber2(action.phoneNumber)
            is AddEmployeeUiAction.OnPositionChanged -> updatePosition(action.position)
            is AddEmployeeUiAction.OnEmailChanged -> updateEmail(action.email)
            is AddEmployeeUiAction.OnUpdateNationality -> updateNationality(action.nationality)
            is AddEmployeeUiAction.OnCityChanged -> updateCity(action.city)
            AddEmployeeUiAction.OnSaveClicked -> onSave()
            else -> Unit
        }
    }

    private fun updateCity(city: String) {
        viewModelScope.launch {
            _uiState.update { oldState ->
                oldState.copy(employee = oldState.employee.copy(city = city))
            }
        }
    }

    private fun updateNationality(nationality: String) {
        viewModelScope.launch {
            _uiState.update { oldState ->
                oldState.copy(employee = oldState.employee.copy(nationality = nationality))
            }
        }
    }

    private fun updateEmail(email: String) {
        viewModelScope.launch {
            _uiState.update { oldState ->
                oldState.copy(employee = oldState.employee.copy(email = email))
            }
        }
    }

    private fun onSave() {
        viewModelScope.launch {
            with(uiState.value) {
                if (employee.name.isBlank()) {
                    _uiState.update { it.copy(error = AddEmployeeUiError.NAME_IS_REQUIRED) }
                    return@launch
                }
                if (employee.email.isNotBlank() && !InputValidator.isEmailValid(employee.email)) {
                    _uiState.update { it.copy(error = AddEmployeeUiError.EMAIL_IS_INVALID) }
                    return@launch
                }
                if (isDriver && employee.nationality.isBlank()) {
                    _uiState.update { it.copy(error = AddEmployeeUiError.NATIONALITY_IS_REQUIRED) }
                    return@launch
                }
                if (employee.phoneNumber1.isBlank()) {
                    _uiState.update { it.copy(error = AddEmployeeUiError.PHONE_NUMBER_IS_REQUIRED) }
                    return@launch
                }
                if (!InputValidator.isPhoneNumberValid(employee.phoneNumber1)) {
                    _uiState.update { it.copy(error = AddEmployeeUiError.PHONE_NUMBER_1_IS_INVALID) }
                    return@launch
                }
                if (employee.phoneNumber2.isNotBlank() && !InputValidator.isPhoneNumberValid(
                        employee.phoneNumber2
                    )
                ) {
                    _uiState.update { it.copy(error = AddEmployeeUiError.PHONE_NUMBER_2_IS_INVALID) }
                    return@launch
                }
                _uiState.update { it.copy(error = AddEmployeeUiError.NONE) }
            }
            if (uiState.value.isNew) {
                createEmployee()
            } else {
                updateEmployee()
            }
        }
    }

    private fun updateEmployee() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                employeeRepo.updateEmployee(uiState.value.employee).collect { result ->
                    result.onSuccess { isUpdated ->
                        if (isUpdated) {
                            _uiState.update { it.copy(isFinished = true) }
                        } else {
                            _uiState.update { it.copy(error = AddEmployeeUiError.NAME_OR_PHONE_ARE_ALREADY_USED) }
                        }
                    }.onFailure { error ->
                        Log.e(TAG, "updateCompany: ${error.message}")
                        error.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "updateCompany: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private suspend fun createEmployee() {
        viewModelScope.launch(Dispatchers.IO) {
            employeeRepo.createEmployee(
                uiState.value.employee.copy(
                    createdAtEpochSeconds = Clock.System.now().epochSeconds
                )
            ).collect { result ->
                result.onSuccess { isCreated ->
                    if (isCreated) {
                        _uiState.update { it.copy(isFinished = true) }
                    } else {
                        _uiState.update { it.copy(error = AddEmployeeUiError.NAME_OR_PHONE_ARE_ALREADY_USED) }
                    }
                }.onFailure { error ->
                    Log.e(TAG, "createCompany: ${error.message}")
                    error.printStackTrace()
                }
            }
        }
    }

    private fun updatePosition(position: String) {
        _uiState.update {
            it.copy(employee = it.employee.copy(position = position))
        }
    }

    private fun updatePhoneNumber1(phoneNumber: String) {
        viewModelScope.launch {
            _uiState.update { oldState ->
                oldState.copy(employee = oldState.employee.copy(phoneNumber1 = phoneNumber))
            }
        }
    }

    private fun updatePhoneNumber2(phoneNumber: String) {
        viewModelScope.launch {
            _uiState.update { oldState ->
                oldState.copy(employee = oldState.employee.copy(phoneNumber2 = phoneNumber))
            }
        }
    }

    private fun updateName(name: String) {
        viewModelScope.launch {
            _uiState.update { oldState ->
                oldState.copy(employee = oldState.employee.copy(name = name))
            }
        }
    }

    private fun updateCompany(company: Company) {
        viewModelScope.launch {
            _uiState.update { oldState ->
                oldState.copy(
                    employee = oldState.employee.copy(company = company.name, companyId = company.id)
                )
            }
        }
    }

}