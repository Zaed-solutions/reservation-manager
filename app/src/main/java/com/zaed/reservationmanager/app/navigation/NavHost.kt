package com.zaed.reservationmanager.app.navigation

import CustomerListScreen
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.compiler.plugins.kotlin.EmptyFunctionMetrics.composable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.CompanyType
import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.ui.addcompany.AddCompanyScreen
import com.zaed.reservationmanager.ui.client.create.NewClientDataEntryScreen
import com.zaed.reservationmanager.ui.addemployee.AddEmployeeScreen
import com.zaed.reservationmanager.ui.companies.CompaniesScreen
import com.zaed.reservationmanager.ui.employee.EmployeeListScreen
import com.zaed.reservationmanager.ui.reservation.create.CreateReservationScreen
import kotlin.reflect.typeOf

@Composable
fun NavigationHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Route.CreateReservationRoute,
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
        composable<Route.CreateReservationRoute> {
            CreateReservationScreen(
                navigateBack = { navController.popBackStack() }
            )
        }
        composable<Route.EmployeeListRoute>{
            EmployeeListScreen(
                navigateBack = { navController.popBackStack() }
            )
        }
        composable<Route.CustomerListRoute> {
            CustomerListScreen(
                navigateBack = { navController.popBackStack() }
            )
        }
        composable<Route.AddCompanyRoute> (
            typeMap = mapOf(
                typeOf<Company>() to CustomNavType.CompanyType
            )
        ){ backStackEntry ->
            val company = backStackEntry.toRoute<Route.AddCompanyRoute>().company
            AddCompanyScreen (
                initialCompany = company,
                onBackPressed = { navController.popBackStack() }
            )
        }
        composable<Route.CompaniesScreen> {
            CompaniesScreen(
                onShowNavDrawer = { /*TODO*/ },
                onNavigateToEditCompany = { company ->
                    navController.navigate(Route.AddCompanyRoute(company))
                },
                onNavigateToDetails = { /*TODO*/},
                onNavigateToAddCompany = {
                    navController.navigate(Route.AddCompanyRoute())
                }
            )
        }
        composable<Route.NewCLientRoute> {
            NewClientDataEntryScreen(
                navigateBack = { navController.popBackStack() }
            )
        }
        composable<Route.AddEmployeeRoute>(
            typeMap = mapOf(
                typeOf<Employee>() to CustomNavType.EmployeeType
            )
        ){ backStackEntry ->
            val args = backStackEntry.toRoute<Route.AddEmployeeRoute>()
            AddEmployeeScreen(
                initialEmployee = args.employee,
                isDriver = args.isDriver,
                onBackPressed = { navController.popBackStack() }
            )
        }
    }
}