package com.zaed.reservationmanager.ui.client

sealed interface CreateCustomerUiAction {
    data class UpdateName(val name: String) : CreateCustomerUiAction
    data class UpdateNationality(val nationality: String) : CreateCustomerUiAction
    data class UpdateNumber(val number: String) : CreateCustomerUiAction
    data class UpdateCountry(val country: String) : CreateCustomerUiAction
    data class UpdateEmail(val email: String) : CreateCustomerUiAction
    data object AddClient : CreateCustomerUiAction
    data object Cancel : CreateCustomerUiAction
    data object DismissStatusError : CreateCustomerUiAction


}
