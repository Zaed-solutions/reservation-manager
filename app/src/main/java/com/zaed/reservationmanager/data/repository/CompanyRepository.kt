package com.zaed.reservationmanager.data.repository

import com.zaed.reservationmanager.data.model.Company
import kotlinx.coroutines.flow.Flow

interface CompanyRepository {
    fun createCompany(company: Company): Flow<Result<Boolean>>
    fun updateCompany(company: Company): Flow<Result<Unit>>
    fun deleteCompany(companyId: String): Flow<Result<Unit>>
    fun getCompanies(): Flow<Result<List<Company>>>
    fun getCompaniesNames(isDriver: Boolean): Flow<Result<List<String>>>
}