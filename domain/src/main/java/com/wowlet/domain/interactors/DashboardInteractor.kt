package com.wowlet.domain.interactors

import com.wowlet.entities.Result
import com.wowlet.entities.local.*
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey

interface DashboardInteractor {
    fun generateQRrCode(list: List<WalletItem>): List<EnterWallet>
    suspend fun getWallets(): Result<YourWallets>
    suspend fun getYourWallets(): YourWallets
    suspend fun getAddCoinList(): AddCoinModel
    suspend fun clearSecretKey()
    suspend fun addCoin(addCoinItem: AddCoinItem): Result<Boolean>
    fun showSelectedMintAddress(addCoinItem: AddCoinItem): List<AddCoinItem>
}