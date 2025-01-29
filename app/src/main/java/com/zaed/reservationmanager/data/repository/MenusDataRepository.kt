package com.zaed.reservationmanager.data.repository

import com.zaed.reservationmanager.data.model.Menu
import kotlinx.coroutines.flow.Flow

interface MenusDataRepository {
    fun getMenuByName(menuName :Menus):Flow<Result<Menu>>
    suspend fun addItemToMenu(menuName: Menus, item: String): Result<Unit>
    suspend fun removeItemFromMenu(menuName: Menus, item: String): Result<Unit>
}

enum class Menus{
    RESERVATION_TYPES,
    CAR_TYPES,
    COUNTRIES
}