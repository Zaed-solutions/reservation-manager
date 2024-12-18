package com.zaed.reservationmanager.ui.company.add

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.CompanyType
import com.zaed.reservationmanager.data.repository.CompanyRepository
import com.zaed.reservationmanager.ui.util.InputValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class AddCompanyViewModel(
    private val companyRepo: CompanyRepository
) : ViewModel() {
    private val TAG = "AddCompanyViewModel"
    private val _uiState = MutableStateFlow(AddCompanyUiState())
    val uiState = _uiState.asStateFlow()
    fun init(initialCompany: Company) {
        _uiState.update {
            it.copy(isNew = initialCompany.id.isBlank(), company = initialCompany)
        }
    }

    fun handleAction(action: AddCompanyUiAction) {
        when (action) {
            is AddCompanyUiAction.OnCountryChanged -> onUpdateCountry(action.country)
            is AddCompanyUiAction.OnEmailChanged -> onUpdateEmail(action.email)
            is AddCompanyUiAction.OnFaxNumberChanged -> onUpdateFaxNumber(action.faxNumber)
            is AddCompanyUiAction.OnNameChanged -> onUpdateName(action.name)
            is AddCompanyUiAction.OnPhoneNumberChanged -> onUpdatePhoneNumber(action.phoneNumber)
            AddCompanyUiAction.OnSaveClicked -> onSave()
            is AddCompanyUiAction.OnTypeChanged -> onUpdateType(action.index)
            else -> Unit
        }
    }

    private fun onSave() {
        Log.d(TAG, "onSave: ${uiState.value.company}")
        viewModelScope.launch {
            with(uiState.value) {
                if (company.name.isBlank()) {
                    _uiState.update { it.copy(error = AddCompanyUiError.NAME_IS_REQUIRED) }
                    return@launch
                }
                if (company.country.isBlank()) {
                    _uiState.update { it.copy(error = AddCompanyUiError.COUNTRY_IS_REQUIRED) }
                    return@launch
                }
                if (company.email.isNotBlank() && !InputValidator.isEmailValid(company.email)) {
                    _uiState.update { it.copy(error = AddCompanyUiError.EMAIL_IS_INVALID) }
                    return@launch
                }
                if (company.faxNumber.isNotBlank() && !InputValidator.isFaxNumberValid(company.faxNumber)) {
                    _uiState.update { it.copy(error = AddCompanyUiError.FAX_NUMBER_IS_INVALID) }
                    return@launch
                }
                if (company.phoneNumber.isNotBlank() && !InputValidator.isPhoneNumberValid(company.phoneNumber)) {
                    _uiState.update { it.copy(error = AddCompanyUiError.PHONE_NUMBER_IS_INVALID) }
                    return@launch
                }
                _uiState.update { it.copy(error = AddCompanyUiError.NONE) }
            }
            if (uiState.value.isNew) {
                createCompany()
            } else {
                updateCompany()
            }
        }
    }

    private fun updateCompany() {
        viewModelScope.launch(Dispatchers.IO) {
            companyRepo.updateCompany(uiState.value.company).collect { result ->
                result.onSuccess {
                    _uiState.update { it.copy(isFinished = true) }
                }.onFailure { error ->
                    Log.e(TAG, "onSave: ${error.message}")
                    error.printStackTrace()
                }
            }
        }
    }

    private suspend fun createCompany() {
        Log.d(TAG, "createCompany: ${uiState.value.company}")
        viewModelScope.launch(Dispatchers.IO) {
            companyRepo.createCompany(
                uiState.value.company.copy(
                    createdAtEpochSeconds = Clock.System.now().epochSeconds
                )
            ).collect { result ->
                result.onSuccess { isCreated ->
                    if (isCreated) {
                        _uiState.update { it.copy(isFinished = true) }
                    } else {
                        _uiState.update { it.copy(error = AddCompanyUiError.NAME_IS_ALREADY_USED) }
                    }
                }.onFailure { error ->
                    Log.e(TAG, "onSave: ${error.message}")
                    error.printStackTrace()
                }
            }
        }
    }

    private fun onUpdateType(index: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(company = it.company.copy(type = CompanyType.entries[index])) }
        }
    }

    private fun onUpdatePhoneNumber(phoneNumber: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(company = it.company.copy(phoneNumber = phoneNumber)) }
        }
    }

    private fun onUpdateName(name: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(company = it.company.copy(name = name)) }
        }
    }

    private fun onUpdateFaxNumber(faxNumber: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(company = it.company.copy(faxNumber = faxNumber)) }
        }
    }

    private fun onUpdateEmail(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(company = it.company.copy(email = email)) }
        }
    }

    private fun onUpdateCountry(country: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(company = it.company.copy(country = country)) }
        }
    }
}