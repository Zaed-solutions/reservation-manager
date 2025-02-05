package com.zaed.reservationmanager.ui.employee.add

import com.zaed.reservationmanager.data.model.Company

sealed interface AddEmployeeUiAction {
    data object OnBackPressed : AddEmployeeUiAction
    data object OnSaveClicked : AddEmployeeUiAction
    data class OnNameChanged(val name: String) : AddEmployeeUiAction
    data class OnCompanyChanged(val company: Company) : AddEmployeeUiAction
    data class OnCarChanged(val car: String) : AddEmployeeUiAction
    data class OnPositionChanged(val position: String) : AddEmployeeUiAction
    data class OnUpdateNationality(val nationality: String) : AddEmployeeUiAction
    data class OnEmailChanged(val email: String) : AddEmployeeUiAction
    data class OnPhoneNumber1Changed(val phoneNumber: String) : AddEmployeeUiAction
    data class OnPhoneNumber2Changed(val phoneNumber: String) : AddEmployeeUiAction
    data class OnCityChanged(val city: String) : AddEmployeeUiAction
}