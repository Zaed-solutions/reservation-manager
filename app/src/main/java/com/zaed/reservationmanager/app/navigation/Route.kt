package com.zaed.reservationmanager.app.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object DefaultRoute : Route
    @Serializable
    data object AddCompanyRoute: Route
}