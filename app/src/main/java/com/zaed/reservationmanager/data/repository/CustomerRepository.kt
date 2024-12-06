package com.zaed.reservationmanager.data.repository

import com.zaed.reservationmanager.data.model.Customer
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    fun createCustomer(customer: Customer): Flow<Result<Unit>>
    fun updateCustomer(customer: Customer): Flow<Result<Unit>>
    fun deleteCustomer(customerId: String): Flow<Result<Unit>>
    fun getCustomers(): Flow<Result<List<Customer>>>
}