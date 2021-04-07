package com.p2p.wowlet.domain.interactors

import com.p2p.wowlet.entities.Result
import com.p2p.wowlet.entities.local.AddCoinItem
import com.p2p.wowlet.entities.enums.SelectedCurrency
import com.p2p.wowlet.entities.local.AddCoinModel
import com.p2p.wowlet.entities.local.ConstWalletItem
import com.p2p.wowlet.entities.local.EnterWallet
import com.p2p.wowlet.entities.local.LocalWalletItem
import com.p2p.wowlet.entities.local.WalletItem
import com.p2p.wowlet.entities.local.YourWallets
import kotlinx.coroutines.flow.Flow

interface DashboardInteractor {
    fun generateQRrCode(list: List<WalletItem>): List<EnterWallet>
    suspend fun getWallets(): Result<YourWallets>
    suspend fun getAllWallets(): Result<List<WalletItem>>
    suspend fun getYourWallets(): YourWallets
    suspend fun getAddCoinList(): AddCoinModel
    suspend fun clearSecretKey()
    suspend fun addCoin(addCoinItem: AddCoinItem): Result<Boolean>
    fun showSelectedMintAddress(addCoinItem: AddCoinItem): List<AddCoinItem>
    fun setSelectedCurrency(currency: SelectedCurrency)
    fun getSelectedCurrency(): SelectedCurrency?
    suspend fun saveEditedWallet(localWalletItem: LocalWalletItem): Flow<List<WalletItem>>
    fun checkWalletFromList(mintAddress: String): Result<ConstWalletItem>
}