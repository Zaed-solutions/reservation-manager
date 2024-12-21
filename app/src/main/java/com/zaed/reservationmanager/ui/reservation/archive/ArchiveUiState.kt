package com.zaed.reservationmanager.ui.reservation.archive

import com.zaed.reservationmanager.data.model.Reservation

data class ArchiveUiState(
    val reservations: List<Reservation> = emptyList()
)
