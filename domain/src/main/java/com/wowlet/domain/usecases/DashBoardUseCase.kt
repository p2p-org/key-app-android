package com.wowlet.domain.usecases

import com.github.mikephil.charting.data.PieEntry
import com.wowlet.data.datastore.DashboardRepository
import com.wowlet.data.datastore.PreferenceService
import com.wowlet.data.datastore.WowletApiCallRepository
import com.wowlet.domain.extentions.fromConstWalletToAddCoinItem
import com.wowlet.domain.extentions.transferInfoToActivityItem
import com.wowlet.domain.extentions.walletItemToQrCode
import com.wowlet.domain.extentions.walletToWallet
import com.wowlet.domain.interactors.DashboardInteractor
import com.wowlet.entities.CallException
import com.wowlet.entities.Constants
import com.wowlet.entities.Constants.Companion.ERROR_NULL_DATA
import com.wowlet.entities.Result
import com.wowlet.entities.local.*
import kotlinx.coroutines.*
import org.bitcoinj.core.Base58
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey


class DashBoardUseCase(
    private val dashboardRepository: DashboardRepository,
    private val wowletApiCallRepository: WowletApiCallRepository,
    private val preferenceService: PreferenceService
) : DashboardInteractor {
    private var walletData: MutableList<WalletItem> = mutableListOf()
    private var sendCoinWalletList: MutableList<WalletItem> = mutableListOf()
    private var pirChatList: MutableList<PieEntry> = mutableListOf()
    private var yourWalletBalance: Double = 0.0
    private var addCoinData: MutableList<AddCoinItem> = mutableListOf() // 30
    private var minBalance: Long = 0

    val ownerAccountAddress = "22CbwPktYBVbTctjfsr35ozanxwfVjNbBofsnWY4C2YR"
    override fun generateQRrCode(list: List<WalletItem>): List<EnterWallet> {
        return list.map {
            it.walletItemToQrCode(dashboardRepository.getQrCode(it.depositAddress))
        }

    }

    override suspend fun getYourWallets(): YourWallets {
        return YourWallets(sendCoinWalletList, yourWalletBalance, mutableListOf(), mutableListOf())
    }

    override suspend fun getWallets(): Result<YourWallets> {
        val publicKey = preferenceService.getActiveWallet()?.publicKey ?: ""

        try {
            val balance = wowletApiCallRepository.getBalance(publicKey)
            val walletsList = wowletApiCallRepository.getWallets(publicKey).apply {
                add(0, BalanceInfo(publicKey, balance, "SOLMINT", publicKey, 9))
            }
            walletData.clear()
            sendCoinWalletList.clear()
            yourWalletBalance = 0.0
            getConcatWalletItem(walletsList)
            walletData.removeAll { it.depositAddress.isEmpty() }
            sendCoinWalletList.addAll(walletData)
            sendCoinWalletList.removeAll { it.amount == 0.0 }
            walletData.forEach {
                yourWalletBalance += it.price
            }
            val mainWalletData = if (walletData.size > 4)
                walletData.take(4)
            else
                walletData

            walletData.forEach {
                if (it.price.toFloat() != 0.0f)
                    pirChatList.add(PieEntry(it.price.toFloat()))
            }
            return Result.Success(
                YourWallets(
                    walletData,
                    yourWalletBalance,
                    mainWalletData,
                    pirChatList
                )
            )
        } catch (e: Exception) {
            return Result.Error(CallException(Constants.ERROR_TIME_OUT, e.message))
        }

    }

    override suspend fun getAllWallets(): Result<List<WalletItem>> {
        return if (walletData.isNotEmpty()) Result.Success(walletData)
        else Result.Error(CallException(Constants.ERROR_EMPTY_ALL_WALLETS))
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

            val wallets = getYourWallets().wallets
            for (wallet in wallets) {
                for (addCoinItem in addCoinData) {
                    val isAlreadyAdded = addCoinItem.tokenSymbol == wallet.tokenSymbol
                    if (isAlreadyAdded) {
                        addCoinItem.isAlreadyAdded = true
                    }
                }
            }

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

    override suspend fun addCoin(addCoinItem: AddCoinItem): Result<AddCoinItem> {
        val secretKey = preferenceService.getActiveWallet()?.secretKey
        val payer = Account(Base58.decode(secretKey))
        val mintAddress = PublicKey(addCoinItem.mintAddress)
        val newAccount = Account()
        val activeWallet = preferenceService.getActiveWallet()
        val fromPublicKey = activeWallet?.publicKey!!
        try {
            val signature = wowletApiCallRepository.createAndInitializeTokenAccount(
                payer,
                mintAddress,
                newAccount
            )
            repeat(1000) {
                delay(120_000)
                val transaction = wowletApiCallRepository.getConfirmedTransaction(
                    signature,
                    0
                )?.transferInfoToActivityItem(fromPublicKey, "", "","")
                return Result.Success(addCoinItem)
            }
            return Result.Error(CallException(Constants.REQUEST_EXACTION, ""))
        } catch (e: java.lang.Exception) {
            return Result.Error(CallException(Constants.REQUEST_EXACTION, e.message))
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
