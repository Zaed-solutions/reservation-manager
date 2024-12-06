package com.zaed.reservationmanager.data.repository

import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.data.source.remote.EmployeeRemoteDataSource
import kotlinx.coroutines.flow.Flow

class EmployeeRepositoryImpl (
    private val remoteSource: EmployeeRemoteDataSource
): EmployeeRepository {
    override fun createEmployee(employee: Employee): Flow<Result<Unit>> {
        return remoteSource.createEmployee(employee)
    }

    override fun updateEmployee(employee: Employee): Flow<Result<Unit>> {
        return remoteSource.updateEmployee(employee)
    }

    override fun deleteEmployee(employeeId: String): Flow<Result<Unit>> {
        return remoteSource.deleteEmployee(employeeId)
    }

    override fun getEmployees(): Flow<Result<List<Employee>>> {
        return remoteSource.getEmployees()
    }
}