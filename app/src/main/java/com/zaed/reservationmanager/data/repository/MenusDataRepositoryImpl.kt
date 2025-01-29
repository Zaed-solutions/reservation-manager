package com.zaed.reservationmanager.data.repository

import com.zaed.reservationmanager.data.model.Menu
import com.zaed.reservationmanager.data.source.remote.menus.MenusDataRemoteDataSource
import kotlinx.coroutines.flow.Flow

class MenusDataRepositoryImpl(
    private val menusDataRemoteDataSource: MenusDataRemoteDataSource
) : MenusDataRepository {
    override fun getMenuByName(menuName: Menus) =
        menusDataRemoteDataSource.getMenuByName(menuName)

    override suspend fun addItemToMenu(menuName: Menus, item: String) =
        menusDataRemoteDataSource.addItemToMenu(menuName, item)

    override suspend fun removeItemFromMenu(menuName: Menus, item: String) =
        menusDataRemoteDataSource.removeItemFromMenu(menuName,item)
}