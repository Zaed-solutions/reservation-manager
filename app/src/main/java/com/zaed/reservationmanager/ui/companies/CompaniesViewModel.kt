package com.zaed.reservationmanager.ui.companies

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaed.reservationmanager.data.model.CompanyType
import com.zaed.reservationmanager.data.repository.CompanyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CompaniesViewModel(
    private val companyRepo: CompanyRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(CompaniesUiState())
    val uiState = _uiState.asStateFlow()
    private val TAG = "CompaniesViewModel"
    init {
        fetchCompanies()
    }

    private fun fetchCompanies() {
        viewModelScope.launch(Dispatchers.IO) {
            companyRepo.getCompanies().collect { result ->
                result.onSuccess { data ->
                    val groupedCompanies = data.groupBy { it.type }
                    _uiState.update { oldState ->
                        oldState.copy(
                            isLoading = false,
                            tourismCompanies = groupedCompanies.getOrDefault(CompanyType.TOURISM, emptyList()),
                            travelCompanies = groupedCompanies.getOrDefault(CompanyType.TRAVEL, emptyList()),
                        )
                    }
                }.onFailure { error ->
                    Log.e(TAG, error.message.toString())
                    error.printStackTrace()
                }
            }
        }
    }

    fun handleAction(action: CompaniesUiAction) {
        when(action){
            is CompaniesUiAction.OnDeleteCompanyConfirmed -> deleteCompany(action.companyId)
            else -> Unit
        }
    }

    private fun deleteCompany(companyId: String) {
        viewModelScope.launch (Dispatchers.IO){
            companyRepo.deleteCompany(companyId).collect{ result ->
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