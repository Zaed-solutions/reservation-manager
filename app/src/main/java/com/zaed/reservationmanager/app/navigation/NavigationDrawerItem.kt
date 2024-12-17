package com.zaed.reservationmanager.app.navigation

import androidx.annotation.StringRes
import com.zaed.reservationmanager.R

enum class NavigationDrawerItem(@StringRes val titleRes: Int, val route: Route) {
    RESERVATIONS(R.string.reservations, Route.HomeRoute),
    CUSTOMERS(R.string.customers, Route.CustomerListRoute),
    COMPANIES(R.string.companies, Route.CompaniesScreen),
    EMPLOYEES(R.string.employees, Route.EmployeeListRoute),
    DRIVERS(R.string.drivers, Route.DriversListRoute),
    DROP_DOWN_LISTS(R.string.drop_down_lists, Route.DropDownMenuLists)
}