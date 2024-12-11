package com.zaed.reservationmanager.data.repository

import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.source.remote.CompanyRemoteDataSource
import kotlinx.coroutines.flow.Flow

class CompanyRepositoryImpl (
    private val remoteSource: CompanyRemoteDataSource
): CompanyRepository {
    override fun createCompany(company: Company): Flow<Result<Boolean>> {
        return remoteSource.createCompany(company)
    }

    override fun updateCompany(company: Company): Flow<Result<Unit>> {
        return remoteSource.updateCompany(company)
    }

    override fun deleteCompany(companyId: String): Flow<Result<Unit>> {
        return remoteSource.deleteCompany(companyId)
    }

    override fun getCompanies(): Flow<Result<List<Company>>> {
        return remoteSource.getCompanies()
    }

    override fun getCompanies(isDriver: Boolean): Flow<Result<List<Company>>> {
        return remoteSource.getCompanies(isDriver)
    }

    override fun getCompaniesNames(isDriver: Boolean): Flow<Result<List<String>>> {
        return remoteSource.getCompaniesNames(isDriver)
    }
}