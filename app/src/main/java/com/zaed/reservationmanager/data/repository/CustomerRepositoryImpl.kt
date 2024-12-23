package com.zaed.reservationmanager.data.repository

import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.data.source.remote.CustomerRemoteDataSource
import kotlinx.coroutines.flow.Flow

class CustomerRepositoryImpl(
    private val remoteSource: CustomerRemoteDataSource
) : CustomerRepository {
    override fun createCustomer(customer: Customer): Flow<Result<Pair<Boolean, String>>> {
        return remoteSource.createCustomer(customer)
    }

    override fun addCustomers(customers: List<Customer>): Flow<Result<Unit>> {
        return remoteSource.addCustomers(customers)
    }

    override fun updateCustomers(customers: List<Customer>): Flow<Result<Unit>> {
        return remoteSource.updateCustomers(customers)
    }

    override fun updateCustomer(customer: Customer): Flow<Result<Boolean>> {
        return remoteSource.updateCustomer(customer)
    }

    override fun deleteCustomer(customerId: String): Flow<Result<Boolean>> {
        return remoteSource.deleteCustomer(customerId)
    }

    override fun getCustomers(): Flow<Result<List<Customer>>> {
        return remoteSource.getCustomers()
    }

    override suspend fun getCustomerByNumber(number: String): Result<Customer> {
        return remoteSource.getCustomerByNumber(number)
    }

    override suspend fun getCustomerById(id: String): Result<Customer> {
        return remoteSource.getCustomerById(id)
    }
}