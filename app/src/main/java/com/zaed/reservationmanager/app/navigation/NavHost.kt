package com.zaed.reservationmanager.app.navigation

import CustomerListScreen
import UpdateDropDownListsScreen
import android.util.Log
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
import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.data.model.ReservationModel
import com.zaed.reservationmanager.ui.client.create.AddCustomerScreen
import com.zaed.reservationmanager.ui.client.details.CustomerDetailScreen
import com.zaed.reservationmanager.ui.company.add.AddCompanyScreen
import com.zaed.reservationmanager.ui.company.details.CompanyDetailsScreen
import com.zaed.reservationmanager.ui.company.display.CompaniesScreen
import com.zaed.reservationmanager.ui.driver.DriverListScreen
import com.zaed.reservationmanager.ui.employee.add.AddEmployeeScreen
import com.zaed.reservationmanager.ui.employee.display.EmployeeListScreen
import com.zaed.reservationmanager.ui.reservation.create.CreateReservationScreen
import com.zaed.reservationmanager.ui.home.HomeScreen
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
        startDestination = Route.HomeRoute,
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
        composable<Route.DropDownMenuLists>(){
            UpdateDropDownListsScreen(
                onNavDrawerClicked = { onShowNavDrawer() }
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
                onNavigateToDetails = { companyId, isTravel ->
                    navController.navigate(Route.CompanyDetailsRoute(companyId, isTravel))
                },
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
        composable<Route.HomeRoute> {
            HomeScreen (
                onShowNavDrawer = { onShowNavDrawer() },
                onNavigateToCompanyDetails = { companyId, companyType ->
                    navController.navigate(Route.CompanyDetailsRoute(companyId, companyType))
                },
                onNavigateToEditCustomer = {
                    navController.navigate(Route.AddCustomerRoute(it))
                },
                onNavigateToCustomerDetails = {
                    navController.navigate(Route.CustomerDetailsRoute(it))
                },
                onNavigateToAddCustomer = {
                    navController.navigate(Route.AddCustomerRoute())
                },
                onNavigateToAddReservation = {
                    navController.navigate(Route.CreateReservationRoute())
                }
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
                    navController.navigate(Route.CustomerDetailsRoute(it))
                }
            )
        }

        composable<Route.DriversListRoute> {
            DriverListScreen (
                onShowNavDrawer = { onShowNavDrawer() },
                onNavigateToAddDriver = {
                    navController.navigate(Route.AddEmployeeRoute(isDriver = true))
                },
                onNavigateToEmployeeDetails = {},
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
                onNavigateToEmployeeDetails = {},
                onNavigateToEditEmployee = { employee ->
                    navController.navigate(Route.AddEmployeeRoute(employee, isDriver = false))
                }
            )
        }
        composable<Route.CreateReservationRoute> (
            typeMap = mapOf(
                typeOf<ReservationModel>() to CustomNavType.ReservationType,
                typeOf<Customer>() to CustomNavType.CustomerType
            )
        ){
            val args = it.toRoute<Route.CreateReservationRoute>()
            CreateReservationScreen(
                reservation = args.reservation,
                initialCustomer = args.initialCustomer,
                navigateBack = { navController.popBackStack() }
            )
        }

        composable<Route.CompanyDetailsRoute>{ backStackEntry ->
            val args = backStackEntry.toRoute<Route.CompanyDetailsRoute>()
            CompanyDetailsScreen(
                companyId = args.companyId,
                companyType = args.companyType,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCompanyDetails = { companyId, companyType ->
                    navController.navigate(Route.CompanyDetailsRoute(companyId, companyType))
                },
            )
        }
        composable<Route.CustomerDetailsRoute> { backStackEntry ->
            val customerId = backStackEntry.toRoute<Route.CustomerDetailsRoute>().customerId
            Log.d(TAG, "NavigationHost: navigate to customer details with: $customerId")
            CustomerDetailScreen(
                customerId = customerId,
                onBackPressed = { navController.popBackStack() },
                onNavigateToCompanyDetails = { companyId, companyType ->
                    navController.navigate(Route.CompanyDetailsRoute(companyId, companyType))
                }
            )
        }
    }
}

