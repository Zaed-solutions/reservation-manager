package com.zaed.reservationmanager.ui.home.component

import androidx.annotation.StringRes
import com.zaed.reservationmanager.R

sealed interface TimeFilter {
    data object All: TimeFilter
    data object Yesterday: TimeFilter
    data object Today: TimeFilter
    data object Tomorrow: TimeFilter
    data object TodayOnwards: TimeFilter
    data class FixedDate(val date: Long): TimeFilter
    data class FixedRange(val startDate: Long, val endDate: Long): TimeFilter
}
enum class TimeFilters(@StringRes val titleRes: Int, val filter: TimeFilter){
    YESTERDAY(titleRes = R.string.yesterday, TimeFilter.Yesterday),
    TODAY(titleRes = R.string.today, TimeFilter.Today),
    TOMORROW(titleRes = R.string.tomorrow, TimeFilter.Tomorrow),
    FROM_TODAY_ONWARDS(titleRes = R.string.from_today_onwards, TimeFilter.TodayOnwards);
}