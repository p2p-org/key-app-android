package com.wowlet.domain.usecases

import android.graphics.Bitmap
import com.wowlet.data.datastore.DashboardRepository
import com.wowlet.data.datastore.PreferenceService
import com.wowlet.data.datastore.WowletApiCallRepository
import com.wowlet.domain.extentions.constWalletToWallet
import com.wowlet.domain.interactors.DashboardInteractor
import com.wowlet.entities.CallException
import com.wowlet.entities.Constants.Companion.ERROR_NULL_DATA
import com.wowlet.entities.Result
import com.wowlet.entities.local.BalanceInfo
import com.wowlet.entities.local.ConstWalletItem
import com.wowlet.entities.local.WalletItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope


class DashBoardUseCase(
    private val dashboardRepository: DashboardRepository,
    private val wowletApiCallRepository: WowletApiCallRepository,
    private val preferenceService: PreferenceService
) : DashboardInteractor {
    private var walletData: MutableList<WalletItem> = mutableListOf()
    override fun generateQRrCode(): Bitmap {
        val publickKey = preferenceService.getSecretDataAtFile()?.publicKey ?: ""
        return dashboardRepository.getQrCode(publickKey)
    }

    override suspend fun getWallets(): List<WalletItem> {
        val publicKey = preferenceService.getSecretDataAtFile()?.publicKey ?: ""
        val walletsList = wowletApiCallRepository.getWallets(publicKey)
        getConcatWalletItem(walletsList)
        return walletData
    }

    override  fun getAddCoinList(): List<ConstWalletItem> {
        // val publicKey = preferenceService.getSecretDataAtFile()?.publicKey ?: ""
        // val walletsList = wowletApiCallRepository.getWallets(publicKey)
        // getConcatWalletItem(walletsList)
        return dashboardRepository.getConstWallets()
    }

    private suspend fun getConcatWalletItem(walletsList: List<BalanceInfo>) {
        walletData.addAll(dashboardRepository.getConstWallets().map {
            it.constWalletToWallet(walletsList)
        })

        coroutineScope {
            walletData.map { walletsItem ->
                async(Dispatchers.IO) {
                    val symbol = walletsItem.tokenSymbol.replace("/", "")
                    when (val overbookData =
                        wowletApiCallRepository.getOrderBooks(walletsItem.tokenSymbol + "USDT")) {
                        is Result.Success -> {
                            overbookData.data?.let {
                                if (it.success && it.data.bids.isNotEmpty()) {
                                    val price = it.data.bids[0].price
                                    walletsItem.price = price * walletsItem.tkns
                                }
                            }
                        }
                        else -> {
                            Result.Error(
                                CallException(
                                    ERROR_NULL_DATA,
                                    null,
                                    "Can't load OrderBook data at server"
                                )
                            )
                        }
                    }
                }
            }.awaitAll()
        }
    }
}
