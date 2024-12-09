package com.zaed.reservationmanager.data.source.remote

import com.zaed.reservationmanager.data.model.Employee
import kotlinx.coroutines.flow.Flow

interface EmployeeRemoteDataSource {
    fun createEmployee(employee: Employee): Flow<Result<Boolean>>
    fun updateEmployee(employee: Employee): Flow<Result<Unit>>
    fun deleteEmployee(employeeId: String): Flow<Result<Unit>>
    fun getEmployees(): Flow<Result<List<Employee>>>
}