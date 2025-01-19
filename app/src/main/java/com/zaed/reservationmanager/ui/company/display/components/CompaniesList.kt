package com.zaed.reservationmanager.ui.company.display.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.CompanyType
import com.zaed.reservationmanager.data.model.CompanyWithBalance
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme

@Composable
fun CompaniesList(
    modifier: Modifier = Modifier,
    companies: List<CompanyWithBalance> = emptyList(),
    onNavigateToCompanyDetails: (companyId: String, companyType: CompanyType) -> Unit = { _, _ -> },
    onDeleteCompany: (companyId: String) -> Unit = {},
    onEditCompany: (company: Company) -> Unit = {},
    onCopyPhone: (String) -> Unit = {},
    onMessagePhone: (String) -> Unit = {},
    onSaveToContacts: (company: Company) -> Unit = {},
) {

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = companies,
            key = { it.company.id }
        ) { companyWithBalance ->
            val company = companyWithBalance.company
            ExpandableCompanyItem(
                modifier = Modifier.animateItem(),
                companyWithBalance = companyWithBalance,
                onCompanyDetailsClicked = {
                    onNavigateToCompanyDetails(company.id, company.type)
                },
                onDeleteCompany = {
                    onDeleteCompany(company.id)
                },
                onEditCompany = {
                    onEditCompany(company)
                },
                onCopyPhone = {
                    onCopyPhone(it)
                },
                onMessagePhone = {
                    onMessagePhone(it)
                },
                onSaveToContacts = {
                    onSaveToContacts(company)
                }
            )
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun CompaniesListPreview() {
    ReservationManagerTheme {
        val companies = listOf(
            CompanyWithBalance(
            Company(
                id = "1",
                name = "Company 1",
                country = "Egypt",
                phoneNumber1 = "+01012345678",
                faxNumber = "+1234567890",
                email = "company-1@test.com"
            ),50),
            CompanyWithBalance(
            Company(
                id = "2",
                name = "Company 2",
                country = "Egypt",
                phoneNumber1 = "+01012345678",
                faxNumber = "+1234567890",
                email = "company-2@test.com"
            ),600),
        )
        CompaniesList(
            modifier = Modifier.padding(horizontal = 16.dp),
            companies = companies
        )
    }
}