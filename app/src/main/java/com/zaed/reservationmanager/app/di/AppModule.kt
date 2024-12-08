package com.zaed.reservationmanager.app.di

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.zaed.reservationmanager.data.repository.CompanyRepository
import com.zaed.reservationmanager.data.repository.CompanyRepositoryImpl
import com.zaed.reservationmanager.data.repository.CustomerRepository
import com.zaed.reservationmanager.data.repository.CustomerRepositoryImpl
import com.zaed.reservationmanager.data.repository.EmployeeRepository
import com.zaed.reservationmanager.data.repository.EmployeeRepositoryImpl
import com.zaed.reservationmanager.data.source.remote.CompanyRemoteDataSource
import com.zaed.reservationmanager.data.source.remote.CompanyRemoteDataSourceImpl
import com.zaed.reservationmanager.data.source.remote.CustomerRemoteDataSource
import com.zaed.reservationmanager.data.source.remote.CustomerRemoteDataSourceImpl
import com.zaed.reservationmanager.data.source.remote.EmployeeRemoteDataSource
import com.zaed.reservationmanager.data.source.remote.EmployeeRemoteDataSourceImpl
import com.zaed.reservationmanager.ui.addcompany.AddCompanyViewModel
import com.zaed.reservationmanager.ui.client.CreateCustomerViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    includes(repositoryModule, viewModelModule, remoteModule)
}

val viewModelModule = module {
    viewModelOf(::AddCompanyViewModel)
    viewModelOf(::CreateCustomerViewModel)
}

val repositoryModule = module {
    singleOf(::CompanyRepositoryImpl) { bind<CompanyRepository>() }
    singleOf(::CustomerRepositoryImpl) { bind<CustomerRepository>() }
    singleOf(::EmployeeRepositoryImpl) { bind<EmployeeRepository>() }
}

val remoteModule = module {
    singleOf(::CompanyRemoteDataSourceImpl) { bind<CompanyRemoteDataSource>() }
    singleOf(::CustomerRemoteDataSourceImpl) { bind<CustomerRemoteDataSource>() }
    singleOf(::EmployeeRemoteDataSourceImpl) { bind<EmployeeRemoteDataSource>() }
    single<FirebaseFirestore> { Firebase.firestore }
}