package com.zaed.reservationmanager.app.navigation

import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.Employee
import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data class AddCompanyRoute(val company: Company = Company()): Route
    @Serializable
    data class AddEmployeeRoute(val employee: Employee = Employee()): Route
    @Serializable
    data object CompaniesScreen: Route
    @Serializable
    data object NewCLientRoute: Route
}