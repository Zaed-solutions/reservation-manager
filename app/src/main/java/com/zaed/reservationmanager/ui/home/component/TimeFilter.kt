package com.zaed.reservationmanager.ui.home.component

sealed interface TimeFilter {
    data object All: TimeFilter
    data object Yesterday: TimeFilter
    data object Today: TimeFilter
    data object Tomorrow: TimeFilter
    data object TodayOnwards: TimeFilter
    data class FixedDate(val date: Long): TimeFilter
    data class FixedRange(val startDate: Long, val endDate: Long): TimeFilter
}