package com.zaed.reservationmanager.data.repository

import com.zaed.reservationmanager.data.model.Employee
import kotlinx.coroutines.flow.Flow

interface EmployeeRepository {
    fun createEmployee(employee: Employee): Flow<Result<Boolean>>
    fun updateEmployee(employee: Employee): Flow<Result<Unit>>
    fun deleteEmployee(employeeId: String): Flow<Result<Unit>>
    fun getEmployees(): Flow<Result<List<Employee>>>
    fun getDrivers(): Flow<Result<List<Employee>>>
    fun getEmployeesByCompany(companyId: String): Flow<Result<List<Employee>>>

}