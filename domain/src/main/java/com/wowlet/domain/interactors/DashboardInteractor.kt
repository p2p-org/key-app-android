package com.wowlet.domain.interactors

import com.wowlet.entities.Result
import com.wowlet.entities.enums.SelectedCurrency
import com.wowlet.entities.local.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey

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