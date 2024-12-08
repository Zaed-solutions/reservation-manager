package com.zaed.reservationmanager.app.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object AddCompanyRoute: Route
    @Serializable
    data object NewCLientRoute: Route
}