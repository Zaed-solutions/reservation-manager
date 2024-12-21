package com.zaed.reservationmanager.app.navigation

import androidx.annotation.StringRes
import com.zaed.reservationmanager.R

enum class NavigationDrawerItem(@StringRes val titleRes: Int, val route: Route) {
    HOME(R.string.home, Route.HomeRoute),
    ARCHIVE(R.string.archive, Route.ArchiveScreen),
    COMPANIES(R.string.companies, Route.CompaniesScreen),
    EMPLOYEES(R.string.employees, Route.EmployeeListRoute),
    DRIVERS(R.string.drivers, Route.DriversListRoute),
    MESSAGES(R.string.messages, Route.MessagesRoute),
    DROP_DOWN_LISTS(R.string.drop_down_lists, Route.DropDownMenuLists)
}