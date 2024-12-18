package com.zaed.reservationmanager.app.navigation

import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.CompanyType
import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.data.model.Employee
import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data class AddCompanyRoute(val company: Company = Company()): Route

    @Serializable
    data class AddEmployeeRoute(val employee: Employee = Employee(), val isDriver: Boolean = false): Route

    @Serializable
    data object CompaniesScreen: Route

    @Serializable
    data class AddCustomerRoute(val customer: Customer = Customer()): Route

    @Serializable
    data object ReservationListRoute: Route

    @Serializable
    data object DriversListRoute: Route

    @Serializable
    data object EmployeeListRoute: Route

    @Serializable
    data object CreateReservationRoute: Route


    @Serializable
    data object HomeRoute: Route

    @Serializable
    data object DropDownMenuLists: Route

    @Serializable
    data class CompanyDetailsRoute(val companyId: String = "", val companyType: CompanyType): Route

    @Serializable
    data class CustomerDetailsRoute(val customerId: String = ""): Route

}