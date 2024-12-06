package com.zaed.reservationmanager.data.source.remote

import com.zaed.reservationmanager.data.model.Company
import kotlinx.coroutines.flow.Flow

interface CompanyRemoteDataSource {
    fun createCompany(company: Company): Flow<Result<Unit>>
    fun updateCompany(company: Company): Flow<Result<Unit>>
    fun deleteCompany(companyId: String): Flow<Result<Unit>>
    fun getCompanies(): Flow<Result<List<Company>>>
}