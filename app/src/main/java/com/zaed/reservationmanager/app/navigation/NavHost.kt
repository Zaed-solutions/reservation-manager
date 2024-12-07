package com.zaed.reservationmanager.app.navigation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.CompanyType
import com.zaed.reservationmanager.ui.addcompany.AddCompanyScreen
import java.util.Date

@Composable
fun NavigationHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Route.AddCompanyRoute,
        enterTransition = {
            fadeIn(
                animationSpec = tween(
                    500, easing = LinearEasing
                )
            )
        },
        exitTransition = {
            fadeOut(
                animationSpec = tween(
                    500, easing = LinearEasing
                )
            )
        }
    ) {
        composable<Route.AddCompanyRoute> {
            val company = Company(
                id = "Nqlxzvb1m4wZ3i5a6aWq",
                name = "Test Company",
                country = "Egypt",
                phoneNumber = "+201012345678",
                email = "mohamed@test.com",
                faxNumber = "+123456789",
                createdAt = Date(),
                type = CompanyType.TOURISM
            )
            AddCompanyScreen (
                initialCompany = company,
                onBackPressed = { navController.popBackStack() }
            )
        }
    }
}