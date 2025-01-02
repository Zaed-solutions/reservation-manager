package com.zaed.reservationmanager.ui.client.create

sealed interface CreateCustomerUiAction {
    data class UpdateName(val name: String) : CreateCustomerUiAction
    data class UpdateNationality(val nationality: String) : CreateCustomerUiAction
    data class UpdateNumber1(val number: String) : CreateCustomerUiAction
    data class UpdateNumber2(val number: String) : CreateCustomerUiAction
    data class UpdateCity(val city: String) : CreateCustomerUiAction
    data class UpdateCountry(val country: String) : CreateCustomerUiAction
    data class UpdateEmail(val email: String) : CreateCustomerUiAction
    data object SubmitClient : CreateCustomerUiAction
}
