package com.wowlet.domain.interactors

import com.wowlet.entities.local.*

interface DashboardInteractor {
    fun generateQRrCode(): EnterWallet
    suspend fun getWallets(): YourWallets
    suspend fun getAddCoinList(): AddCoinModel
    suspend fun clearSecretKey()
    fun showSelectedMintAddress(addCoinItem: AddCoinItem):List<AddCoinItem>
}