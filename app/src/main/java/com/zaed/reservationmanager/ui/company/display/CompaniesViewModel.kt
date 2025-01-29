package com.zaed.reservationmanager.ui.company.display

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaed.reservationmanager.data.model.CompanyType
import com.zaed.reservationmanager.data.model.CompanyWithBalance
import com.zaed.reservationmanager.data.repository.CompanyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CompaniesViewModel(
    private val companyRepo: CompanyRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CompaniesUiState())
    val uiState = _uiState.asStateFlow()
    private val TAG = "CompaniesViewModel"

    init {
        fetchCompanies()
    }

    private fun fetchCompanies() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            companyRepo.getCompanies().collect { result ->
                result.onSuccess { data ->
                    // Fetch balances concurrently
                    val companiesWithBalance = data.map { company ->
                        async {
                            companyRepo.getCompanyBalance(company.id, company.type).fold(
                                onSuccess = { balance ->
                                    CompanyWithBalance(
                                        company,
                                        balance=(balance.totalRidePrice - balance.totalCollected - balance.totalPayment).toInt()
                                    )
                                },
                                onFailure = {
                                    Log.e(TAG, "Failed to fetch balance for company ${company.id}: ${it.message}")
                                    CompanyWithBalance(
                                        company,
                                        balance = 0
                                    ) // Return the original company without a balance
                                }
                            )
                        }
                    }.awaitAll().sortedByDescending { it.balance }

                    // Filter processed companies
                    val tourismCompanies = companiesWithBalance.filter { it.company.type == CompanyType.TOURISM }
                    val travelCompanies = companiesWithBalance.filter { it.company.type == CompanyType.TRAVEL }

                    // Update UI state
                    _uiState.update { oldState ->
                        oldState.copy(
                            isLoading = false,
                            tourismCompanies = tourismCompanies,
                            displayTourismCompanies = tourismCompanies,
                            travelCompanies = travelCompanies,
                            displayTravelCompanies = travelCompanies
                        )
                    }
                }.onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    Log.e(TAG, error.message.toString())
                    error.printStackTrace()
                }
            }
        }
    }


    fun handleAction(action: CompaniesUiAction) {
        when (action) {
            is CompaniesUiAction.OnDeleteCompanyConfirmed -> deleteCompany(action.companyId)
            is CompaniesUiAction.UpdateSearchQuery -> filterData(action.query)
            is CompaniesUiAction.OnFilterCompanies -> filterData(companyFilter = action.filter)
            else -> Unit
        }
    }

    private fun filterData(
        searchQuery: String = uiState.value.searchQuery,
        companyFilter: CompanyFilter = uiState.value.companyFilter
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    searchQuery = searchQuery,
                    companyFilter = companyFilter
                )
            }
        }
        if (searchQuery.isBlank() && companyFilter == CompanyFilter.ALL_ACCOUNTS) {
            Log.d(TAG, "filterData: searchQuery is blank and timeFilter is All")
            _uiState.update {
                it.copy(
                    displayTourismCompanies = it.tourismCompanies,
                    displayTravelCompanies = it.travelCompanies
                )
            }
        } else if(searchQuery.isBlank()){
            _uiState.update {
                it.copy(
                    displayTourismCompanies = uiState.value.tourismCompanies.filter { companyWithBalance ->
                        companyWithBalance.balance != 0
                    },
                    displayTravelCompanies = uiState.value.travelCompanies.filter { companyWithBalance ->
                        companyWithBalance.balance != 0
                    }
                )
            }
        } else if (companyFilter == CompanyFilter.ALL_ACCOUNTS) {
            Log.d(TAG, "filterData: timeFilter is All")
            val filteredTourismCompanies = uiState.value.tourismCompanies.filter { companyWithBalance ->
                listOf(
                    companyWithBalance.company.name,
                    companyWithBalance.company.country,
                    companyWithBalance.company.city,
                    companyWithBalance.company.phoneNumber1,
                    companyWithBalance.company.phoneNumber2
                ).any { value ->
                    value.contains(searchQuery, ignoreCase = true)
                }
            }
            val filteredTravelCompanies = uiState.value.travelCompanies.filter { companyWithBalance ->
                listOf(
                    companyWithBalance.company.name,
                    companyWithBalance.company.country,
                    companyWithBalance.company.city,
                    companyWithBalance.company.phoneNumber1,
                    companyWithBalance.company.phoneNumber2,
                ).any { value ->
                    value.contains(searchQuery, ignoreCase = true)
                }
            }
            _uiState.update { oldState ->
                oldState.copy(
                    displayTourismCompanies = filteredTourismCompanies,
                    displayTravelCompanies = filteredTravelCompanies
                )
            }
        } else {
            val filteredTourismCompanies = uiState.value.tourismCompanies.filter { companyWithBalance ->
                companyWithBalance.balance != 0 || listOf(
                    companyWithBalance.company.name,
                    companyWithBalance.company.country,
                    companyWithBalance.company.city,
                    companyWithBalance.company.phoneNumber1,
                    companyWithBalance.company.phoneNumber2
                ).any { value ->
                    value.contains(searchQuery, ignoreCase = true)
                }
            }
            val filteredTravelCompanies = uiState.value.travelCompanies.filter { companyWithBalance ->
                companyWithBalance.balance != 0 || listOf(
                    companyWithBalance.company.name,
                    companyWithBalance.company.country,
                    companyWithBalance.company.city,
                    companyWithBalance.company.phoneNumber1,
                    companyWithBalance.company.phoneNumber2,
                ).any { value ->
                    value.contains(searchQuery, ignoreCase = true)
                }
            }
            _uiState.update { oldState ->
                oldState.copy(
                    displayTourismCompanies = filteredTourismCompanies,
                    displayTravelCompanies = filteredTravelCompanies
                )
            }
        }
    }

    private fun deleteCompany(companyId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            companyRepo.deleteCompany(companyId).collect { result ->
                result.onSuccess {
                    Log.d(TAG, "Company deleted successfully")
                }.onFailure {
                    Log.e(TAG, it.message.toString())
                    it.printStackTrace()
                }
            }
        }
    }

}