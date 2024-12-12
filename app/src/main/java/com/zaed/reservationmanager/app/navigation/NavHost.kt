package com.zaed.reservationmanager.app.navigation

import CustomerListScreen
import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.ui.client.create.NewClientDataEntryScreen
import com.zaed.reservationmanager.ui.company.add.AddCompanyScreen
import com.zaed.reservationmanager.ui.company.display.CompaniesScreen
import com.zaed.reservationmanager.ui.employee.add.AddEmployeeScreen
import com.zaed.reservationmanager.ui.employee.display.EmployeeListScreen
import com.zaed.reservationmanager.ui.reservation.create.CreateReservationScreen
import com.zaed.reservationmanager.ui.reservation.details.ReservationDetailsScreen
import kotlin.reflect.typeOf

private const val TAG: String = "NavigationHost"

@Composable
fun NavigationHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Route.ReservationDetailsRoute(reservationId = "T6uc2xT8I2SD5dDRcQmJ"),
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
        composable<Route.EmployeeListRoute> {
            EmployeeListScreen(
                navigateBack = { navController.popBackStack() }
            )
        }
        composable<Route.CustomerListRoute> {
            CustomerListScreen(
                navigateBack = { navController.popBackStack() }
            )
        }
        composable<Route.AddCompanyRoute>(
            typeMap = mapOf(
                typeOf<Company>() to CustomNavType.CompanyType
            )
        ) { backStackEntry ->
            val company = backStackEntry.toRoute<Route.AddCompanyRoute>().company
            AddCompanyScreen(
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
                onNavigateToDetails = { /*TODO*/ },
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
        ) { backStackEntry ->
            val args = backStackEntry.toRoute<Route.AddEmployeeRoute>()
            AddEmployeeScreen(
                initialEmployee = args.employee,
                isDriver = args.isDriver,
                onBackPressed = { navController.popBackStack() }
            )
        }
        composable<Route.ReservationDetailsRoute> { navBackStackEntry ->
            val reservationId =
                navBackStackEntry.toRoute<Route.ReservationDetailsRoute>().reservationId
            ReservationDetailsScreen(
                reservationId = reservationId,
                onBackPressed = { navController.popBackStack() },
                onNavigateToCompanyDetails = { companyId, isTravel ->
                    Log.d(TAG, "navigate to company details with: $companyId, $isTravel")
                    /*TODO*/
                },
                onNavigateToClientDetails = { clientId ->
                    Log.d(TAG, "NavigationHost: navigate to client details with: $clientId")
                    /*TODO*/
                },
                onNavigateToEmployeeDetails = { employeeId, isDriver ->
                    Log.d(
                        TAG,
                        "NavigationHost: navigate to employee details with: $employeeId, $isDriver"
                    )
                    /*TODO*/
                }
            )
        }
    }
}