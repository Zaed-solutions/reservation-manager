package com.zaed.reservationmanager.data.repository

import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.CompanyBalance
import com.zaed.reservationmanager.data.model.CompanyPayment
import com.zaed.reservationmanager.data.model.CompanyType
import kotlinx.coroutines.flow.Flow

interface CompanyRepository {
    fun createCompany(company: Company): Flow<Result<Pair<Boolean,String>>>
    fun updateCompany(company: Company): Flow<Result<Pair<Boolean,String>>>
    fun deleteCompany(companyId: String): Flow<Result<Unit>>
    fun getCompanyById(companyId: String): Flow<Result<Company>>
    fun getCompanies(): Flow<Result<List<Company>>>
    fun getTravelCompanies(): Flow<Result<List<Company>>>
    fun getCompaniesNames(isDriver: Boolean): Flow<Result<List<String>>>
    fun getCompanies(isDriver: Boolean): Flow<Result<List<Company>>>
    suspend fun getCompanyBalance(companyId: String, companyType: CompanyType): Result<CompanyBalance>
    fun getCompanyPayments(companyId: String): Flow<Result<List<CompanyPayment>>>
    suspend fun addPayment(payment: CompanyPayment): Result<Boolean>
    suspend fun editPayment(payment: CompanyPayment): Result<Boolean>
    suspend fun deletePayment(paymentId: String): Result<Boolean>
}