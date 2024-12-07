package com.zaed.reservationmanager.ui.addcompany

sealed interface AddCompanyUiAction {
    data object OnBackPressed : AddCompanyUiAction
    data object OnSaveClicked : AddCompanyUiAction
    data class OnNameChanged(val name: String) : AddCompanyUiAction
    data class OnEmailChanged(val email: String) : AddCompanyUiAction
    data class OnFaxNumberChanged(val faxNumber: String) : AddCompanyUiAction
    data class OnPhoneNumberChanged(val phoneNumber: String) : AddCompanyUiAction
    data class OnTypeChanged(val index: Int) : AddCompanyUiAction
    data class OnCountryChanged(val country: String) : AddCompanyUiAction
}