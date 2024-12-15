package com.zaed.reservationmanager.ui.company.display

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.CompanyType
import com.zaed.reservationmanager.ui.company.display.components.CompaniesList
import com.zaed.reservationmanager.ui.company.display.components.ConfirmDeleteDialog
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun CompaniesScreen(
    modifier: Modifier = Modifier,
    viewModel: CompaniesViewModel = koinViewModel(),
    onShowNavDrawer: () -> Unit,
    onNavigateToEditCompany: (company: Company) -> Unit,
    onNavigateToDetails: (companyId: String, companyType: CompanyType) -> Unit,
    onNavigateToAddCompany: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CompaniesScreenContent(
        modifier = modifier,
        onAction = { action ->
            when (action) {
                CompaniesUiAction.OnShowNavDrawer -> onShowNavDrawer()
                is CompaniesUiAction.OnCompanyDetailsClicked -> onNavigateToDetails(action.companyId, action.companyType)
                is CompaniesUiAction.OnEditCompanyClicked -> onNavigateToEditCompany(action.company)
                CompaniesUiAction.OnAddCompanyClicked -> onNavigateToAddCompany()
                else -> viewModel.handleAction(action)
            }
        },
        tourismCompanies = state.tourismCompanies,
        travelCompanies = state.travelCompanies,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompaniesScreenContent(
    modifier: Modifier = Modifier,
    onAction: (CompaniesUiAction) -> Unit = {},
    tourismCompanies: List<Company> = emptyList(),
    travelCompanies: List<Company> = emptyList()
) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { CompanyType.entries.size })
    var clickedCompanyId by remember {
        mutableStateOf("")
    }
    var isConfirmDeleteDialogVisible by remember {
        mutableStateOf(false)
    }
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = { onAction(CompaniesUiAction.OnAddCompanyClicked) }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.companies),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onAction(CompaniesUiAction.OnShowNavDrawer) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = null
                        )
                    }
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            TabRow(selectedTabIndex = pagerState.currentPage) {
                CompanyType.entries.take(2).forEach { companyType ->
                    Tab(
                        selected = pagerState.currentPage == companyType.ordinal,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(companyType.ordinal)
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(companyType.displayNameRes),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) {
                when (pagerState.currentPage) {
                    0 -> {
                        CompaniesList(
                            companies = tourismCompanies,
                            onNavigateToCompanyDetails = { companyId, companyType ->
                                onAction(CompaniesUiAction.OnCompanyDetailsClicked(companyId, companyType))
                            },
                            onDeleteCompany = { companyId ->
                                clickedCompanyId = companyId
                                isConfirmDeleteDialogVisible = true
                            },
                            onEditCompany = { companyId ->
                                onAction(CompaniesUiAction.OnEditCompanyClicked(companyId))
                            }
                        )
                    }

                    1 -> {
                        CompaniesList(
                            companies = travelCompanies,
                            onNavigateToCompanyDetails = { companyId, companyType ->
                                onAction(CompaniesUiAction.OnCompanyDetailsClicked(companyId,companyType))
                            },
                            onDeleteCompany = { companyId ->
                                clickedCompanyId = companyId
                                isConfirmDeleteDialogVisible = true
                            },
                            onEditCompany = { company ->
                                onAction(CompaniesUiAction.OnEditCompanyClicked(company))
                            }
                        )
                    }
                }
            }
            AnimatedVisibility(visible = isConfirmDeleteDialogVisible) {
                ModalBottomSheet(
                    onDismissRequest = {
                        isConfirmDeleteDialogVisible = false
                        clickedCompanyId = ""
                    },
                    sheetState = rememberModalBottomSheetState()
                ) {
                    ConfirmDeleteDialog(
                        label = stringResource(id = R.string.company),
                        onDismiss = {
                            isConfirmDeleteDialogVisible = false
                            clickedCompanyId = ""
                        },
                        onConfirm = {
                            onAction(CompaniesUiAction.OnDeleteCompanyConfirmed(clickedCompanyId))
                            isConfirmDeleteDialogVisible = false
                            clickedCompanyId = ""
                        }
                    )
                }

            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun CompaniesScreenContentPreview() {
    val companies = listOf(
        Company(
            id = "1",
            name = "Company 1",
            country = "Egypt",
            phoneNumber = "+01012345678",
            faxNumber = "+1234567890",
            email = "company-1@test.com"
        ),
        Company(
            id = "2",
            name = "Company 2",
            country = "Egypt",
            phoneNumber = "+01012345678",
            faxNumber = "+1234567890",
            email = "company-2@test.com"
        ),
    )
    ReservationManagerTheme {
        CompaniesScreenContent(
            tourismCompanies = companies
        )
    }
}