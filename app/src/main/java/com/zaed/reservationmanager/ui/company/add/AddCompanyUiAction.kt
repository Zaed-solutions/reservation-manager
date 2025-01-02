package com.zaed.reservationmanager.ui.company.add

sealed interface AddCompanyUiAction {
    data object OnBackPressed : AddCompanyUiAction
    data object OnSaveClicked : AddCompanyUiAction
    data class OnNameChanged(val name: String) : AddCompanyUiAction
    data class OnEmailChanged(val email: String) : AddCompanyUiAction
    data class OnFaxNumberChanged(val faxNumber: String) : AddCompanyUiAction
    data class OnPhoneNumber1Changed(val phoneNumber: String) : AddCompanyUiAction
    data class OnPhoneNumber2Changed(val phoneNumber: String) : AddCompanyUiAction
    data class OnTypeChanged(val index: Int) : AddCompanyUiAction
    data class OnCountryChanged(val country: String) : AddCompanyUiAction
}