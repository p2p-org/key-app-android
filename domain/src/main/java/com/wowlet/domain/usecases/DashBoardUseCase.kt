package com.wowlet.domain.usecases

import com.wowlet.data.datastore.DashboardRepository
import com.wowlet.data.datastore.PreferenceService
import com.wowlet.data.datastore.WowletApiCallRepository
import com.wowlet.domain.extentions.fromConstWalletToAddCoinItem
import com.wowlet.domain.extentions.walletItemToQrCode
import com.wowlet.domain.extentions.walletToWallet
import com.wowlet.domain.interactors.DashboardInteractor
import com.wowlet.entities.CallException
import com.wowlet.entities.Constants.Companion.ERROR_NULL_DATA
import com.wowlet.entities.Result
import com.wowlet.entities.local.*
import kotlinx.coroutines.*
import kotlin.math.pow


class DashBoardUseCase(
    private val dashboardRepository: DashboardRepository,
    private val wowletApiCallRepository: WowletApiCallRepository,
    private val preferenceService: PreferenceService
) : DashboardInteractor {
    private var walletData: MutableList<WalletItem> = mutableListOf()
    private var yourWalletBalance: Double = 0.0
    private var addCoinData: MutableList<AddCoinItem> = mutableListOf()
    private var minBalance: Long = 0

    val accountAddress = "3h1zGmCwsRJnVk5BuRNMLsPaQu1y2aqXqXDWYCgrp5UG"
    override fun generateQRrCode(list: List<WalletItem>): List<EnterWallet> {
        val publicKey = preferenceService.getActiveWallet()?.publicKey ?: "No valid key"
        return list.map {
            it.walletItemToQrCode(dashboardRepository.getQrCode(it.depositAddress))
        }

    }

    override suspend fun getYourWallets(): YourWallets {
        val publicKey = preferenceService.getActiveWallet()?.publicKey ?: ""

        walletData.clear()
        yourWalletBalance = 0.0
        val balance = wowletApiCallRepository.getBalance(publicKey)
        val walletsList = wowletApiCallRepository.getWallets(publicKey).apply {
            add(0, BalanceInfo(publicKey, balance, "SOLMINT", publicKey, 9))
        }
        val number: Long = 0
        walletsList.removeAll { it.amount == number }
        getConcatWalletItem(walletsList)
        walletData.forEach { item ->
            yourWalletBalance += item.price
        }
        walletData.removeAll { it.amount == 0.0 }

        return YourWallets(walletData, yourWalletBalance)
    }

    override suspend fun getWallets(): YourWallets {
        val publicKey = preferenceService.getActiveWallet()?.publicKey ?: ""

        walletData.clear()
        yourWalletBalance = 0.0
        val balance = wowletApiCallRepository.getBalance(publicKey)
        val walletsList = wowletApiCallRepository.getWallets(publicKey).apply {
            add(0, BalanceInfo(publicKey, balance, "SOLMINT", publicKey, 9))
        }
        getConcatWalletItem(walletsList)
        walletData.removeAll { it.depositAddress.isEmpty() }
        walletData.forEach {
            yourWalletBalance += it.price
        }

        return YourWallets(walletData, yourWalletBalance)
    }

    override suspend fun getAddCoinList(): AddCoinModel {
        addCoinData.clear()
        coroutineScope {

            dashboardRepository.getConstWallets().map { walletsItem ->

                async(Dispatchers.IO) {
                    when (val historicalPrice =
                        dashboardRepository.getHistoricalPrices(walletsItem.tokenSymbol + "USDT")) {
                        is Result.Success -> {
                            historicalPrice.data?.let {
                                if (it.isNotEmpty()) {
                                    val close = it[0].close
                                    val open = it[0].open
                                    val change24h = close - open
                                    val change24hInPercentages = if (open == 0.0) {
                                        change24h / 1
                                    } else {
                                        change24h / open
                                    }
                                    addCoinData.add(
                                        walletsItem.fromConstWalletToAddCoinItem(
                                            change24h,
                                            change24hInPercentages
                                        )
                                    )
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

            val minBalance = async(Dispatchers.IO) {
                minBalance = wowletApiCallRepository.getMinimumBalance(165)
            }
            minBalance.await()
        }

        return AddCoinModel(minBalance, addCoinData)
    }

    override fun showSelectedMintAddress(addCoinItem: AddCoinItem): List<AddCoinItem> {
        addCoinData.forEach {
            it.isShowMindAddress = it.tokenSymbol == addCoinItem.tokenSymbol
        }
        return addCoinData
    }


    override suspend fun clearSecretKey() {
        val secretData = preferenceService.getActiveWallet()
        secretData?.let {
            it.secretKey = ""
            preferenceService.updateWallet(it)
            preferenceService.finishLoginReg(false)
            preferenceService.enableFingerPrint(EnableFingerPrintModel(false, false))
            preferenceService.enableNotification(EnableNotificationModel(false, false))
        }
    }

    private suspend fun getConcatWalletItem(walletsList: List<BalanceInfo>) {
        val constData = dashboardRepository.getConstWallets()
        val walletDataTemp = walletsList.map {
            it.walletToWallet(constData)
        }
        walletData.addAll(walletDataTemp)
        coroutineScope {
            walletData.map { walletsItem ->
                async {
                    when (val overbookData =
                        wowletApiCallRepository.getOrderBooks(walletsItem.tokenSymbol + "USDT")) {
                        is Result.Success -> {
                            overbookData.data?.let {
                                if (it.bids.isNotEmpty()) {
                                    val price = it.bids[0].price
                                    walletsItem.walletBinds = price
                                    walletsItem.price = price * walletsItem.amount
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
            }
        }.awaitAll()
    }
}
