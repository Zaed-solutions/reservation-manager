package com.zaed.reservationmanager.data.source.remote.menus

import com.zaed.reservationmanager.data.model.Menu
import com.zaed.reservationmanager.data.repository.Menus
import kotlinx.coroutines.flow.Flow

interface MenusDataRemoteDataSource {
    fun getMenuByName(menuName : Menus): Flow<Result<Menu>>
    suspend fun addItemToMenu(menuName: Menus, item: String): Result<Unit>
    suspend fun removeItemFromMenu(menuName: Menus, item: String) : Result<Unit>
}
