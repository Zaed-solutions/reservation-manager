package com.zaed.reservationmanager.ui.company.display

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.CompanyType
import com.zaed.reservationmanager.data.model.CompanyWithBalance
import com.zaed.reservationmanager.ui.company.display.components.CompaniesList
import com.zaed.reservationmanager.ui.company.display.components.ConfirmDeleteDialog
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme
import com.zaed.reservationmanager.ui.util.PhoneUtil
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
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    CompaniesScreenContent(
        modifier = modifier,
        isLoading = state.isLoading,
        onAction = { action ->
            when (action) {
                CompaniesUiAction.OnShowNavDrawer -> onShowNavDrawer()
                is CompaniesUiAction.OnCompanyDetailsClicked -> onNavigateToDetails(
                    action.companyId,
                    action.companyType
                )

                is CompaniesUiAction.OnEditCompanyClicked -> onNavigateToEditCompany(action.company)
                CompaniesUiAction.OnAddCompanyClicked -> onNavigateToAddCompany()
                is CompaniesUiAction.OnCopyPhoneNumber -> {
                    if (action.phoneNumber.isNotBlank()) {
                        clipboardManager.setText(AnnotatedString(action.phoneNumber))
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = context.getString(R.string.number_copied_to_clipboard),
                                withDismissAction = true
                            )
                        }
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar(context.getString(R.string.phone_number_is_blank))
                        }
                    }
                }

                is CompaniesUiAction.OnMessagePhoneNumber -> {
                    if (action.phoneNumber.isNotBlank()) {
                        PhoneUtil.sendWhatsappMessage(
                            context = context,
                            phoneNumber = action.phoneNumber,
                            message = "",
                            onFailure = {
                                scope.launch {
                                    snackbarHostState.showSnackbar(context.getString(R.string.whatsapp_is_not_installed))
                                }
                            }
                        )
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar(context.getString(R.string.phone_number_is_blank))
                        }
                    }
                }

                is CompaniesUiAction.OnSaveToContacts -> {
                    PhoneUtil.saveToContacts(
                        context,
                        action.company.name,
                        action.company.phoneNumber1,
                        action.company.phoneNumber2,
                        action.company.email
                    )
                }

                else -> viewModel.handleAction(action)
            }
        },
        searchQuery = state.searchQuery,
        companyFilter = state.companyFilter,
        tourismCompanies = state.displayTourismCompanies,
        travelCompanies = state.displayTravelCompanies,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompaniesScreenContent(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    onAction: (CompaniesUiAction) -> Unit = {},
    searchQuery: String = "",
    companyFilter: CompanyFilter = CompanyFilter.ALL_ACCOUNTS,
    tourismCompanies: List<CompanyWithBalance> = emptyList(),
    travelCompanies: List<CompanyWithBalance> = emptyList(),
    snackbarHostState: SnackbarHostState? = null,
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
        snackbarHost = { snackbarHostState?.let { SnackbarHost(it) } },
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
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    val data = if (it.matches(Regex("[+\\d\\s]+"))) it.replace(" ", "") else it
                    onAction(CompaniesUiAction.UpdateSearchQuery(data))
                },
                placeholder = { Text(stringResource(R.string.smart_search)) },
                modifier = Modifier
                    .fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                onAction(CompaniesUiAction.UpdateSearchQuery(""))
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = null
                            )
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.large
            )
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            ) {
                SegmentedButton(
                    selected = companyFilter == CompanyFilter.OPEN_ACCOUNT,
                    onClick = {
                        onAction(CompaniesUiAction.OnFilterCompanies(CompanyFilter.OPEN_ACCOUNT))
                    },
                    shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
                ) {
                    Text(
                        text = stringResource(CompanyFilter.OPEN_ACCOUNT.titleRes),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                SegmentedButton(
                    selected = companyFilter == CompanyFilter.ALL_ACCOUNTS,
                    onClick = {
                        onAction(CompaniesUiAction.OnFilterCompanies(CompanyFilter.ALL_ACCOUNTS))
                    },
                    shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                ) {
                    Text(
                        text = stringResource(CompanyFilter.ALL_ACCOUNTS.titleRes),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            TabRow(
                modifier = Modifier.padding(top = 16.dp),
                selectedTabIndex = pagerState.currentPage
            ) {
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
            AnimatedVisibility(isLoading) {
                LinearProgressIndicator(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp))
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
                                onAction(
                                    CompaniesUiAction.OnCompanyDetailsClicked(
                                        companyId,
                                        companyType
                                    )
                                )
                            },
                            onDeleteCompany = { companyId ->
                                clickedCompanyId = companyId
                                isConfirmDeleteDialogVisible = true
                            },
                            onEditCompany = { companyId ->
                                onAction(CompaniesUiAction.OnEditCompanyClicked(companyId))
                            },
                            onCopyPhone = { onAction(CompaniesUiAction.OnCopyPhoneNumber(it)) },
                            onMessagePhone = { onAction(CompaniesUiAction.OnMessagePhoneNumber(it)) },
                            onSaveToContacts = { onAction(CompaniesUiAction.OnSaveToContacts(it)) }
                        )
                    }

                    1 -> {
                        CompaniesList(
                            companies = travelCompanies,
                            onNavigateToCompanyDetails = { companyId, companyType ->
                                onAction(
                                    CompaniesUiAction.OnCompanyDetailsClicked(
                                        companyId,
                                        companyType
                                    )
                                )
                            },
                            onDeleteCompany = { companyId ->
                                clickedCompanyId = companyId
                                isConfirmDeleteDialogVisible = true
                            },
                            onEditCompany = { company ->
                                onAction(CompaniesUiAction.OnEditCompanyClicked(company))
                            },
                            onCopyPhone = { onAction(CompaniesUiAction.OnCopyPhoneNumber(it)) },
                            onMessagePhone = { onAction(CompaniesUiAction.OnMessagePhoneNumber(it)) },
                            onSaveToContacts = { onAction(CompaniesUiAction.OnSaveToContacts(it)) }
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
        CompanyWithBalance(
            company = Company(
                id = "1",
                name = "Company 1",
                country = "Egypt",
                phoneNumber1 = "+01012345678",
                faxNumber = "+1234567890",
                email = "company-1@test.com"
            )
        ),
        CompanyWithBalance(
            Company(
                id = "2",
                name = "Company 2",
                country = "Egypt",
                phoneNumber1 = "+01012345678",
                faxNumber = "+1234567890",
                email = "company-2@test.com"
            )
        ),
    )
    ReservationManagerTheme {
        CompaniesScreenContent(
            tourismCompanies = companies
        )
    }
}


enum class CompanyFilter(@StringRes val titleRes: Int){
    OPEN_ACCOUNT(R.string.open_account),
    ALL_ACCOUNTS(R.string.all_accounts)
}