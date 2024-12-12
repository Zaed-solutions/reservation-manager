package com.zaed.reservationmanager.app.navigation

import CustomerListScreen
import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.ui.client.create.AddCustomerScreen
import com.zaed.reservationmanager.ui.company.add.AddCompanyScreen
import com.zaed.reservationmanager.ui.company.display.CompaniesScreen
import com.zaed.reservationmanager.ui.driver.DriverListScreen
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
    onShowNavDrawer: () -> Unit,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Route.ReservationListRoute,
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
        composable<Route.CompaniesScreen> {
            CompaniesScreen(
                onShowNavDrawer = { onShowNavDrawer() },
                onNavigateToEditCompany = { company ->
                    navController.navigate(Route.AddCompanyRoute(company))
                },
                onNavigateToDetails = { /*TODO*/ },
                onNavigateToAddCompany = {
                    navController.navigate(Route.AddCompanyRoute())
                }
            )
        }
        composable<Route.AddCustomerRoute> (
            typeMap = mapOf(
                typeOf<Customer>() to CustomNavType.CustomerType
            )
        ){ backStackEntry ->
            val args = backStackEntry.toRoute<Route.AddCustomerRoute>()
            AddCustomerScreen(
                navigateBack = { navController.popBackStack() },
                initialCustomer = args.customer
            )
        }
        composable<Route.CustomerListRoute> {
            CustomerListScreen(
                onShowNavDrawer = { onShowNavDrawer() },
                onNavigateToEditCustomer = { customer ->
                    navController.navigate(Route.AddCustomerRoute(customer))
                },
                onNavigateToAddCustomer = {
                    navController.navigate(Route.AddCustomerRoute())
                },
                onNavigateToCustomerDetails = {
                    /*TODO*/
                }
            )
        }
        composable<Route.ReservationListRoute> {
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                Text(
                    text = "Reservation List Screen",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
        composable<Route.DriversListRoute> {
            DriverListScreen (
                onShowNavDrawer = { onShowNavDrawer() },
                onNavigateToAddDriver = {
                    navController.navigate(Route.AddEmployeeRoute(isDriver = true))
                },
                onNavigateToEmployeeDetails = {
                    /*TODO*/
                },
                onNavigateToEditDriver = { driver ->
                    navController.navigate(Route.AddEmployeeRoute(driver, isDriver = true))
                }
            )
        }
        composable<Route.EmployeeListRoute> {
            EmployeeListScreen(
                onShowNavDrawer = { onShowNavDrawer() },
                onNavigateToAddEmployee = {
                    navController.navigate(Route.AddEmployeeRoute(isDriver = false))
                },
                onNavigateToEmployeeDetails = {
                    /*TODO*/
                },
                onNavigateToEditEmployee = { employee ->
                    navController.navigate(Route.AddEmployeeRoute(employee, isDriver = false))
                }
            )
        }
        composable<Route.CreateReservationRoute> {
            CreateReservationScreen(
                navigateBack = { navController.popBackStack() }
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