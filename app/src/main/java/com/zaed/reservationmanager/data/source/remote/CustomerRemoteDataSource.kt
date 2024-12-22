package com.zaed.reservationmanager.data.source.remote

import com.zaed.reservationmanager.data.model.Customer
import kotlinx.coroutines.flow.Flow

interface CustomerRemoteDataSource {
    fun createCustomer(customer: Customer): Flow<Result<Boolean>>
    fun updateCustomer(customer: Customer): Flow<Result<Boolean>>
    fun deleteCustomer(customerId: String): Flow<Result<Unit>>
    fun getCustomers(): Flow<Result<List<Customer>>>
    suspend fun getCustomerByNumber(number: String): Result<Customer>
    suspend fun getCustomerById(id: String): Result<Customer>
    fun addCustomers(customers: List<Customer>): Flow<Result<Unit>>
    fun updateCustomers(customers: List<Customer>): Flow<Result<Unit>>
}