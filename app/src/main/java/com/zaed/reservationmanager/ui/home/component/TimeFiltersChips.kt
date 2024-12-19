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
                selected = filter.filter::class == selectedTimeFilter::class,
                leadingIcon = {
                    if (filter.filter::class == selectedTimeFilter::class) {
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
                    if (selectedTimeFilter::class ==  TimeFilter.FixedDate::class) {
                        onUpdateTimeFilter(TimeFilter.All)
                    } else {
                        onShowDatePicker()
                    }
                },
                label = { Text(stringResource(R.string.selected_date)) },
                selected = selectedTimeFilter::class ==  TimeFilter.FixedDate::class,
                leadingIcon = {
                    if (selectedTimeFilter::class ==  TimeFilter.FixedDate::class) {
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
                    if (selectedTimeFilter::class == TimeFilter.FixedRange::class) {
                        onUpdateTimeFilter(TimeFilter.All)
                    } else {
                        onShowDateRangePicker()
                    }
                },
                label = { Text(stringResource(R.string.selected_range)) },
                selected = selectedTimeFilter::class == TimeFilter.FixedRange::class,
                leadingIcon = {
                    if (selectedTimeFilter::class == TimeFilter.FixedRange::class) {
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