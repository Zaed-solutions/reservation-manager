package com.zaed.reservationmanager.ui.home.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zaed.reservationmanager.R

@Composable
fun TimeFiltersChips(
    onUpdateTimeFilter: (TimeFilter) -> Unit,
    selectedTimeFilter: TimeFilter,
    onShowDateRangePicker: () -> Unit,
    onShowDatePicker: () -> Unit
) {
    LazyRow {
        items(
            items = TimeFilters.entries
        ) { filter ->
            FilterChip(
                modifier = Modifier.padding(end = 8.dp),
                onClick = {
                    onUpdateTimeFilter(filter.filter)
                },
                label = { Text(stringResource(filter.titleRes)) },
                selected = filter.filter == selectedTimeFilter,
                leadingIcon = {
                    if (selectedTimeFilter == filter.filter) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                },
            )
        }
        item {
            FilterChip(
                modifier = Modifier.padding(end = 8.dp),
                onClick = {
                    if (selectedTimeFilter is TimeFilter.FixedDate) {
                        onUpdateTimeFilter(TimeFilter.All)
                    } else {
                        onShowDatePicker()
                    }
                },
                label = { Text(stringResource(R.string.selected_date)) },
                selected = selectedTimeFilter is TimeFilter.FixedDate,
                leadingIcon = {
                    if (selectedTimeFilter is TimeFilter.FixedDate) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                },
            )
        }
        item {
            FilterChip(
                modifier = Modifier.padding(end = 8.dp),
                onClick = {
                    if (selectedTimeFilter is TimeFilter.FixedRange) {
                        onUpdateTimeFilter(TimeFilter.All)
                    } else {
                        onShowDateRangePicker()
                    }
                },
                label = { Text(stringResource(R.string.selected_range)) },
                selected = selectedTimeFilter is TimeFilter.FixedRange,
                leadingIcon = {
                    if (selectedTimeFilter is TimeFilter.FixedRange) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                },
            )
        }

    }
}