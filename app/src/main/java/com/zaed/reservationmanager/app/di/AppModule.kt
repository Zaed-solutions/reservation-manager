package com.zaed.reservationmanager.app.di

import com.zaed.reservationmanager.ui.dropdownmenu.MenuDataStore
import com.zaed.reservationmanager.ui.dropdownmenu.UpdateDropDownListsViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.zaed.reservationmanager.data.repository.CompanyRepository
import com.zaed.reservationmanager.data.repository.CompanyRepositoryImpl
import com.zaed.reservationmanager.data.repository.CustomerRepository
import com.zaed.reservationmanager.data.repository.CustomerRepositoryImpl
import com.zaed.reservationmanager.data.repository.EmployeeRepository
import com.zaed.reservationmanager.data.repository.EmployeeRepositoryImpl
import com.zaed.reservationmanager.data.repository.ReservationRepository
import com.zaed.reservationmanager.data.repository.ReservationRepositoryImpl
import com.zaed.reservationmanager.data.source.remote.CompanyRemoteDataSource
import com.zaed.reservationmanager.data.source.remote.CompanyRemoteDataSourceImpl
import com.zaed.reservationmanager.data.source.remote.CustomerRemoteDataSource
import com.zaed.reservationmanager.data.source.remote.CustomerRemoteDataSourceImpl
import com.zaed.reservationmanager.data.source.remote.EmployeeRemoteDataSource
import com.zaed.reservationmanager.data.source.remote.EmployeeRemoteDataSourceImpl
import com.zaed.reservationmanager.data.source.remote.ReservationRemoteDataSource
import com.zaed.reservationmanager.data.source.remote.ReservationRemoteDataSourceImpl
import com.zaed.reservationmanager.ui.company.add.AddCompanyViewModel
import com.zaed.reservationmanager.ui.employee.add.AddEmployeeViewModel
import com.zaed.reservationmanager.ui.company.display.CompaniesViewModel
import com.zaed.reservationmanager.ui.client.create.CreateCustomerViewModel
import com.zaed.reservationmanager.ui.client.details.CustomerDetailsViewModel
import com.zaed.reservationmanager.ui.client.display.CustomerListViewModel
import com.zaed.reservationmanager.ui.company.details.CompanyDetailsViewModel
import com.zaed.reservationmanager.ui.driver.DriverListViewModel
import com.zaed.reservationmanager.ui.dropdownmenu.MenuDataStoreImpl
import com.zaed.reservationmanager.ui.employee.display.EmployeeListViewModel
import com.zaed.reservationmanager.ui.reservation.create.CreateReservationViewModel
import com.zaed.reservationmanager.ui.reservation.details.ReservationDetailsViewModel
import com.zaed.reservationmanager.ui.home.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    includes(repositoryModule, viewModelModule, remoteModule)
}

val viewModelModule = module {
    viewModelOf(::AddCompanyViewModel)
    viewModelOf(::CompaniesViewModel)
    viewModelOf(::CreateCustomerViewModel)
    viewModelOf(::CustomerListViewModel)
    viewModelOf(::AddEmployeeViewModel)
    viewModelOf(::EmployeeListViewModel)
    viewModelOf(::DriverListViewModel)
    viewModelOf(::CreateReservationViewModel)
    viewModelOf(::ReservationDetailsViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::UpdateDropDownListsViewModel)
    viewModelOf(::CompanyDetailsViewModel)
    viewModelOf(::CustomerDetailsViewModel)
}

val repositoryModule = module {
    singleOf(::CompanyRepositoryImpl) { bind<CompanyRepository>() }
    singleOf(::CustomerRepositoryImpl) { bind<CustomerRepository>() }
    singleOf(::EmployeeRepositoryImpl) { bind<EmployeeRepository>() }
    singleOf(::ReservationRepositoryImpl) { bind<ReservationRepository>() }
}

val remoteModule = module {
    singleOf(::CompanyRemoteDataSourceImpl) { bind<CompanyRemoteDataSource>() }
    singleOf(::CustomerRemoteDataSourceImpl) { bind<CustomerRemoteDataSource>() }
    singleOf(::EmployeeRemoteDataSourceImpl) { bind<EmployeeRemoteDataSource>() }
    singleOf(::ReservationRemoteDataSourceImpl) { bind<ReservationRemoteDataSource>() }
    singleOf(::MenuDataStoreImpl) { bind<MenuDataStore>() }
    single<FirebaseFirestore> { Firebase.firestore }
}