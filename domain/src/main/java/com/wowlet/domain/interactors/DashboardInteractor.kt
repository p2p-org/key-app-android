package com.wowlet.domain.interactors

import com.wowlet.entities.local.*

interface DashboardInteractor {
    fun generateQRrCode(list: List<WalletItem>): List<EnterWallet>
    suspend fun getWallets(): YourWallets
    suspend fun getYourWallets(): YourWallets
    suspend fun getAddCoinList(): AddCoinModel
    suspend fun clearSecretKey()
    fun showSelectedMintAddress(addCoinItem: AddCoinItem):List<AddCoinItem>
}