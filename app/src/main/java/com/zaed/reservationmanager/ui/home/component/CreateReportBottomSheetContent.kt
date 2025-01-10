package com.zaed.reservationmanager.ui.home.component

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.CompanyHistory
import com.zaed.reservationmanager.data.model.CompanyType
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.ui.components.TitledDropDownTextField
import com.zaed.reservationmanager.ui.reservation.create.component.convertRangeToString
import com.zaed.reservationmanager.ui.reservation.create.component.toSeconds
import com.zaed.reservationmanager.ui.util.SheetUtil
import com.zaed.reservationmanager.ui.util.formatEpochSecondsToDate
import com.zaed.reservationmanager.ui.util.formatEpochSecondsToMonthlyDate
import java.io.File


@Composable
fun CreateReportBottomSheetContent(
    modifier: Modifier = Modifier,
    tourismCompanies: List<Company> = emptyList(),
    travelCompanies: List<Company> = emptyList(),
    cars: List<String> = emptyList(),
    onFetchReservations: (report: Report, onSuccess: (List<Reservation>) -> Unit) -> Unit,
    onFetchCompaniesHistory: (report: Report, onSuccess: (List<CompanyHistory>) -> Unit) -> Unit,
    onDismiss: () -> Unit,
    onShareFile: (File) -> Unit,
    onOpenFile: (File) -> Unit,
) {
    val context = LocalContext.current
    var report by remember {
        mutableStateOf(Report())
    }
    var generatedFile by remember { mutableStateOf<File?>(null) }
    var isSelectCompanyTypeVisible by remember { mutableStateOf(false) }
    var isSelectCompanyVisible by remember { mutableStateOf(false) }
    var isSelectCompanyAccountTypeVisible by remember { mutableStateOf(false) }
    var isSelectCarTypeVisible by remember { mutableStateOf(false) }
    var isSelectCarVisible by remember { mutableStateOf(false) }
    var isExportEnabled by remember { mutableStateOf(false) }
    var isSelectRangeVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isLoaded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedContent(isLoading to isLoaded) { state ->
            when {
                state.first -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp))
                    }
                }

                state.second -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        if (generatedFile == null) {
                            Text(
                                text = stringResource(id = R.string.no_reservations_added),
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        } else {
                            Text(
                                text = stringResource(id = R.string.create_report),
                                style = MaterialTheme.typography.headlineMedium,
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp, top = 24.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    modifier = Modifier.weight(1f),
                                    onClick = { generatedFile?.let { onShareFile(it) } },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = null
                                    )
                                    Text(
                                        modifier = Modifier.padding(start = 4.dp),
                                        text = stringResource(id = R.string.share)
                                    )
                                }
                                Button(
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        generatedFile?.let { onOpenFile(generatedFile!!) }
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FileOpen,
                                        contentDescription = null
                                    )
                                    Text(
                                        modifier = Modifier.padding(start = 4.dp),
                                        text = stringResource(id = R.string.open)
                                    )
                                }
                            }
                        }
                    }
                }

                else -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(id = R.string.create_report),
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        TitledDropDownTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            isClearEnabled = false,
                            title = stringResource(id = R.string.report_type),
                            options = ReportType.entries.map { stringResource(it.stringRes) },
                            onValueChanged = { index ->
                                report = report.copy(type = ReportType.entries[index])
                                when (index) {
                                    ReportType.ALL_ARRIVALS.ordinal -> {
                                        isSelectCarVisible = true
                                        isExportEnabled = false
                                        isSelectRangeVisible = false
                                        isSelectCompanyVisible = false
                                        isSelectCarTypeVisible = false
                                        isSelectCompanyTypeVisible = false
                                        isSelectCompanyAccountTypeVisible = false
                                    }

                                    ReportType.PROFITS.ordinal -> {
                                        isSelectCarVisible = false
                                        isExportEnabled = false
                                        isSelectRangeVisible = true
                                        isSelectCompanyVisible = false
                                        isSelectCarTypeVisible = false
                                        isSelectCompanyTypeVisible = false
                                        isSelectCompanyAccountTypeVisible = false
                                    }

                                    else -> {
                                        isSelectCompanyTypeVisible = true
                                        isSelectCarVisible = false
                                        isExportEnabled = false
                                        isSelectRangeVisible = false
                                        isSelectCompanyVisible = false
                                        isSelectCarTypeVisible = false
                                        isSelectCompanyAccountTypeVisible = false
                                    }
                                }
                            },
                            selectedValue = report.type?.let { stringResource(report.type!!.stringRes) }
                                ?: "",
                        )
                        AnimatedVisibility(isSelectCompanyTypeVisible) {
                            TitledDropDownTextField(
                                isClearEnabled = false,
                                options = CompanyType.entries.map { stringResource(it.displayNameRes) },
                                selectedValue = report.companyType?.displayNameRes?.let { stringResource(it) }?:"",
                                title = stringResource(id = R.string.company_type),
                                onValueChanged = { index ->
                                    report =
                                        report.copy(companyType = CompanyType.entries[index])
                                    if (report.type == ReportType.OPEN_BALANCE) {
                                        isSelectCompanyVisible = false
                                        isSelectCarVisible = false
                                        isExportEnabled = false
                                        isSelectRangeVisible = true
                                        isSelectCarTypeVisible = false
                                        isSelectCompanyAccountTypeVisible = false
                                    } else {
                                        isSelectCompanyVisible = true
                                        isSelectCarVisible = false
                                        isExportEnabled = false
                                        isSelectRangeVisible = false
                                        isSelectCarTypeVisible = false
                                        isSelectCompanyAccountTypeVisible = false
                                    }
                                },
                            )
                        }
                        AnimatedVisibility(isSelectCompanyVisible) {
                            TitledDropDownTextField(
                                isClearEnabled = false,
                                options = when (report.companyType) {
                                    CompanyType.TOURISM -> tourismCompanies.map { it.name }
                                    CompanyType.TRAVEL -> travelCompanies.map { it.name }
                                    else -> emptyList()
                                },
                                selectedValue = report.company.name,
                                title = stringResource(id = R.string.company),
                                onValueChanged = { index ->
                                    report = report.copy(
                                        company = when (report.companyType) {
                                            CompanyType.TOURISM -> tourismCompanies[index]
                                            CompanyType.TRAVEL -> travelCompanies[index]
                                            else -> Company()
                                        }
                                    )
                                    Log.d(
                                        "CompanySelection",
                                        "CreateReportBottomSheetContent: $report"
                                    )
                                    when (report.type) {
                                        ReportType.COMPANY_ACCOUNT -> {
                                            Log.d(
                                                "CompanySelection",
                                                "CreateReportBottomSheetContent: COMPANY_ACCOUNT"
                                            )
                                            isSelectCompanyAccountTypeVisible = true
                                            isExportEnabled = false
                                            isSelectRangeVisible = false
                                            isSelectCarTypeVisible = false
                                            isSelectCarVisible = false
                                        }

                                        else -> {
                                            Log.d(
                                                "CompanySelection",
                                                "CreateReportBottomSheetContent: ELSE"
                                            )
                                            isSelectCarVisible = true
                                            isExportEnabled = false
                                            isSelectRangeVisible = false
                                            isSelectCarTypeVisible = false
                                            isSelectCompanyAccountTypeVisible = false
                                        }
                                    }
                                },
                            )
                        }
                        androidx.compose.animation.AnimatedVisibility(isSelectCarVisible) {
                            TitledDropDownTextField(
                                isClearEnabled = false,
                                options = CarType.entries.map { stringResource(it.stringRes) },
                                selectedValue = report.carType?.let { stringResource(it.stringRes) }
                                    ?: "",
                                title = stringResource(id = R.string.car_filter),
                                onValueChanged = { index ->
                                    val selectedType = CarType.entries[index]
                                    report = report.copy(carType = selectedType)
                                    if (selectedType == CarType.CERTAIN_CAR) {
                                        isSelectCarTypeVisible = true
                                        isExportEnabled = false
                                        isSelectRangeVisible = false
                                    } else {
                                        isSelectRangeVisible = true
                                        isSelectCarTypeVisible = false
                                        isExportEnabled = false
                                    }
                                },
                            )
                        }
                        androidx.compose.animation.AnimatedVisibility(isSelectCarTypeVisible) {
                            TitledDropDownTextField(
                                isClearEnabled = false,
                                options = cars,
                                selectedValue = report.car,
                                title = stringResource(id = R.string.car),
                                onValueChanged = { index ->
                                    report =
                                        report.copy(car = cars[index])
                                    isSelectRangeVisible = true
                                },
                            )
                        }
                        androidx.compose.animation.AnimatedVisibility(
                            isSelectCompanyAccountTypeVisible
                        ) {
                            TitledDropDownTextField(
                                isClearEnabled = false,
                                options = CompanyAccountType.entries.map { stringResource(it.stringRes) },
                                selectedValue = report.companyAccountType?.let { stringResource(it.stringRes) }
                                    ?: "",
                                title = stringResource(id = R.string.account_type),
                                onValueChanged = { index ->
                                    report =
                                        report.copy(companyAccountType = CompanyAccountType.entries[index])
                                    isSelectRangeVisible = true
                                },
                            )
                        }
                        AnimatedVisibility(isSelectRangeVisible) {
                            DateRangePickerField(
                                onDateRangeSelected = { range ->
                                    report = report.copy(
                                        fromEpochSeconds = range.first?.toSeconds() ?: 0L,
                                        toEpochSeconds = range.second?.toSeconds() ?: 0L
                                    )
                                    isExportEnabled = true
                                }
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                modifier = Modifier.weight(1f),
                                onClick = { onDismiss() },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.cancel)
                                )
                            }
                            Button(
                                modifier = Modifier.weight(1f),
                                enabled = isExportEnabled,
                                onClick = {
                                    isLoading = true
                                    when (report.type) {
                                        ReportType.OPEN_BALANCE->{
                                            onFetchCompaniesHistory(
                                                report
                                            ) { data ->
                                                if(data.isEmpty()){
                                                    isLoaded = true
                                                    isLoading = false
                                                }else {
                                                    SheetUtil.generatePaginatedArabicPdfReportForCompanyOpenAccount(
                                                        context = context,
                                                        history = data,
                                                        companyType = report.company.type,
                                                        title = context.getString(
                                                            R.string.open_account_title,
                                                            if(report.companyType ==CompanyType.TRAVEL) context.getString(R.string.travel) else context.getString(R.string.tourism),
                                                            report.fromEpochSeconds.formatEpochSecondsToMonthlyDate(),
                                                            report.toEpochSeconds.formatEpochSecondsToMonthlyDate(),
                                                        ),
                                                    ).let { pdfFile ->
                                                        isLoaded = true
                                                        isLoading = false
                                                        generatedFile = pdfFile
                                                    }
                                                }
                                            }
                                        }
                                        else->{
                                            onFetchReservations(
                                                report
                                            ) { reservations ->
                                                if (reservations.isNotEmpty()) {
                                                    when (report.type) {
                                                        ReportType.COMPANY_ACCOUNT -> {
                                                            val filteredReservation =
                                                                if (report.companyAccountType == CompanyAccountType.OPEN_BALANCE) {
                                                                    reservations.filter {
                                                                        if (report.company.type == CompanyType.TRAVEL)
                                                                            it.travelRidePrice != it.travelCollectedAmount
                                                                        else
                                                                            it.tourismRidePrice != it.tourismCollectedAmount
                                                                    }
                                                                } else {
                                                                    reservations
                                                                }
                                                            SheetUtil.generatePaginatedArabicPdfReportForCompanyAccount(
                                                                context = context,
                                                                reservations = filteredReservation,
                                                                companyType = report.company.type,
                                                                title = context.getString(
                                                                    R.string.company_account_report_placeholder,
                                                                    report.company.name,
                                                                    convertRangeToString(report.fromEpochSeconds to report.toEpochSeconds)
                                                                ),
                                                            ).let { pdfFile ->
                                                                isLoaded = true
                                                                isLoading = false
                                                                generatedFile = pdfFile
                                                            }
                                                        }

                                                        ReportType.COMPANY_ARRIVAL -> {
                                                            SheetUtil.generatePaginatedArabicPdfReportForCompanyArrivals(
                                                                context = context,
                                                                reservations = reservations,
                                                                companyType = report.company.type,
                                                                title = context.getString(
                                                                    R.string.company_arrival_report_placeholder,
                                                                    report.company.name,
                                                                    convertRangeToString(report.fromEpochSeconds to report.toEpochSeconds)
                                                                ),
                                                            )?.let { pdfFile ->
                                                                isLoaded = true
                                                                isLoading = false
                                                                generatedFile = pdfFile
                                                            }
                                                        }

                                                        ReportType.PROFITS -> {
                                                            SheetUtil.generatePaginatedArabicPdfReportForProfits(
                                                                context = context,
                                                                reservations = reservations.sortedBy { it.date + it.time },
                                                                title = context.getString(
                                                                    R.string.profits_report_placeholder,
                                                                    report.fromEpochSeconds.formatEpochSecondsToMonthlyDate(),
                                                                    report.toEpochSeconds.formatEpochSecondsToMonthlyDate()
                                                                ),
                                                            )?.let { pdfFile ->
                                                                isLoaded = true
                                                                isLoading = false
                                                                generatedFile = pdfFile
                                                            }
                                                        }

                                                        else -> {
                                                            SheetUtil.generatePaginatedArabicPdfReportForAllArrivals(
                                                                context = context,
                                                                reservations = reservations,
                                                                title = context.getString(
                                                                    R.string.all_arrival_report_placeholder,
                                                                    convertRangeToString(report.fromEpochSeconds to report.toEpochSeconds)
                                                                ),
                                                            )?.let { pdfFile ->
                                                                isLoaded = true
                                                                isLoading = false
                                                                generatedFile = pdfFile
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    isLoaded = true
                                                    isLoading = false
                                                }
                                            }
                                        }
                                    }

                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(stringResource(R.string.export))
                            }
                        }
                    }
                }
            }
        }

    }
}

data class Report(
    val type: ReportType? = null,
    val companyType: CompanyType? = null,
    val companyAccountType: CompanyAccountType? = null,
    val company: Company = Company(),
    val carType: CarType? = null,
    val car: String = "",
    val fromEpochSeconds: Long = 0,
    val toEpochSeconds: Long = 0,
)

enum class ReportType(@StringRes val stringRes: Int) {
    COMPANY_ACCOUNT(R.string.company_account),
    COMPANY_ARRIVAL(R.string.company_arrival),
    ALL_ARRIVALS(R.string.show_all_arrival),
    PROFITS(R.string.profits),
    OPEN_BALANCE(R.string.open_balance),
}

enum class CompanyAccountType(@StringRes val stringRes: Int) {
    ALL_ARRIVALS(R.string.all_arivals),
    OPEN_BALANCE(R.string.open_balance),
}

enum class CarType(@StringRes val stringRes: Int) {
    CERTAIN_CAR(R.string.certain_car),
    ALL_CARS(R.string.all),
}
