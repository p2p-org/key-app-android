package com.p2p.wallet.dashboard.interactor

import com.github.mikephil.charting.data.PieEntry
import com.p2p.wallet.common.network.CallException
import com.p2p.wallet.common.network.Constants
import com.p2p.wallet.common.network.Constants.Companion.ERROR_NULL_DATA
import com.p2p.wallet.common.network.Result
import com.p2p.wallet.dashboard.model.SelectedCurrency
import com.p2p.wallet.dashboard.model.local.AddCoinItem
import com.p2p.wallet.dashboard.model.local.AddCoinModel
import com.p2p.wallet.dashboard.model.local.BalanceInfo
import com.p2p.wallet.dashboard.model.local.ConstWallet
import com.p2p.wallet.dashboard.model.local.EnterWallet
import com.p2p.wallet.dashboard.model.local.LocalWalletItem
import com.p2p.wallet.dashboard.model.local.YourWallets
import com.p2p.wallet.dashboard.repository.DashboardRepository
import com.p2p.wallet.dashboard.repository.LocalDatabaseRepository
import com.p2p.wallet.dashboard.repository.WowletApiCallRepository
import com.p2p.wallet.dashboard.repository.fromConstWalletToAddCoinItem
import com.p2p.wallet.dashboard.repository.transferInfoToActivityItem
import com.p2p.wallet.dashboard.repository.walletItemToQrCode
import com.p2p.wallet.dashboard.repository.walletToWallet
import com.p2p.wallet.token.model.Token
import com.p2p.wallet.utils.WalletDataConst
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.channelFlow
import org.bitcoinj.core.Base58
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import java.math.BigDecimal
import kotlin.math.pow

@Deprecated("Too complex logic, should be refactored")
class DashboardInteractor(
    private val dashboardRepository: DashboardRepository,
    private val wowletApiCallRepository: WowletApiCallRepository,
    private val localDatabaseRepository: LocalDatabaseRepository
) {
    private var walletData: MutableList<Token> = mutableListOf()
    private var sendCoinWalletList: MutableList<Token> = mutableListOf()
    private var pirChatList: MutableList<PieEntry> = mutableListOf()
    private var yourWalletBalance: Double = 0.0
    private var addCoinData: MutableList<AddCoinItem> = mutableListOf() // 30
    private var minBalance: Long = 0

    val ownerAccountAddress = "22CbwPktYBVbTctjfsr35ozanxwfVjNbBofsnWY4C2YR"
    fun generateQRrCode(list: List<Token>): List<EnterWallet> {
        return list.map {
            it.walletItemToQrCode(dashboardRepository.getQrCode(it.depositAddress))
        }
    }

    suspend fun getYourWallets(): YourWallets {
        return YourWallets(sendCoinWalletList, yourWalletBalance, mutableListOf(), mutableListOf())
    }

    suspend fun getWallets(): Result<YourWallets> {
//        val publicKey = preferenceService.getActiveWallet()?.publicKey ?: ""
        val publicKey = ""

        try {
            val balance = wowletApiCallRepository.getBalance(publicKey)
            val walletsList = wowletApiCallRepository.getWallets(publicKey).apply {
                add(0, BalanceInfo(publicKey, balance, "SOLMINT", publicKey, 9))
            }
            getConcatWalletItem(walletsList)
            walletData.removeAll { it.depositAddress.isEmpty() }
            walletData.forEach { item ->
                val wallet = localDatabaseRepository.getWallet(item.depositAddress)
//                if (wallet != null)
//                    item.tokenName = wallet.walletName
            }
            sendCoinWalletList.addAll(walletData)
            // sendCoinWalletList.removeAll { it.amount == 0.0 }
            walletData.forEach {
                yourWalletBalance += it.price.toDouble()
            }
            val itemComparator = Comparator<Token> { o1, o2 ->
                when {
                    o1?.tokenSymbol == "SOL" -> -1
                    o2?.tokenSymbol == "SOL" -> 1
                    else -> o1?.price?.let { o2?.price?.compareTo(it) } ?: 0
                }
            }

            walletData.sortWith(itemComparator)
            val mainWalletData = if (walletData.size > 4)
                walletData.take(4)
            else
                walletData

            walletData.forEach {
                if (it.price != BigDecimal.ZERO)
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

    suspend fun getAllWallets(): Result<List<Token>> {
        return if (walletData.isNotEmpty()) Result.Success(walletData)
        else Result.Error(CallException(Constants.ERROR_EMPTY_ALL_WALLETS))
    }

    suspend fun getAddCoinList(): AddCoinModel {
        addCoinData.clear()
        coroutineScope {

            dashboardRepository.getConstWallets().map { walletsItem ->

                addCoinData.add(
                    walletsItem.fromConstWalletToAddCoinItem(
                        0.0,
                        0.0,
                        0.0
                    )
                )

//                async(Dispatchers.IO) {
//                    when (val historicalPrice =
//                        dashboardRepository.getHistoricalPrices(walletsItem.tokenSymbol + "USDT")) {
//                        is Result.Success -> {
//                            historicalPrice.data?.let {
//                                if (it.isNotEmpty()) {
//                                    val close = it[0].close
//                                    val open = it[0].open
//                                    val change24h = close - open
//                                    val change24hInPercentages = if (open == 0.0) {
//                                        change24h / 1
//                                    } else {
//                                        change24h / open
//                                    }
//                                    addCoinData.add(
//                                        walletsItem.fromConstWalletToAddCoinItem(
//                                            change24h,
//                                            change24hInPercentages,
//                                            close
//                                        )
//                                    )
//                                }
//                            }
//                        }
//                        else -> {
//                            Result.Error(
//                                CallException(
//                                    ERROR_NULL_DATA,
//                                    null,
//                                    "Can't load OrderBook data at server"
//                                )
//                            )
//                        }
//                    }
//
//                }
            } // .awaitAll()

            for (wallet in walletData) {
                for (addCoinItem in addCoinData) {
                    val isAlreadyAdded = addCoinItem.tokenSymbol == wallet.tokenSymbol
                    if (isAlreadyAdded) {
                        addCoinItem.isAlreadyAdded = true
                    }
                }
            }

            val minBalance = async(Dispatchers.IO) {
                minBalance = wowletApiCallRepository.getMinimumBalance(165)
                addCoinData.forEach {
                    it.minBalance = minBalance.div(10f.pow(9)).toDouble()
                }
            }
            minBalance.await()
        }

        return AddCoinModel(minBalance, addCoinData)
    }

    fun showSelectedMintAddress(addCoinItem: AddCoinItem): List<AddCoinItem> {
        addCoinData.forEach {
            it.isShowMindAddress = it.tokenSymbol == addCoinItem.tokenSymbol
        }
        return addCoinData
    }

    suspend fun clearSecretKey() {
//        val secretData = preferenceService.getActiveWallet()
//        secretData?.let {
//            it.secretKey = ""
//            preferenceService.updateWallet(it.copy(secretKey = ""))
//            preferenceService.finishLoginReg(false)
//            preferenceService.enableFingerPrint(EnableFingerPrintModel(false, false))
//            preferenceService.enableNotification(EnableNotificationModel(false, false))
//        }
    }

    fun setSelectedCurrency(currency: SelectedCurrency) {
//        preferenceService.setSelectedCurrency(currency)
    }

    fun getSelectedCurrency(): SelectedCurrency? {
//        return preferenceService.getSelectedCurrency()
        return null
    }

    suspend fun saveEditedWallet(localWalletItem: LocalWalletItem) =
        channelFlow<List<Token>> {
            localDatabaseRepository.saveEditedWallet(localWalletItem)
            walletData.forEach { item ->
                val wallet = localDatabaseRepository.getWallet(item.depositAddress)
//                if (wallet != null) {
//                    item.tokenName = wallet.walletName
//                }
            }
            channel.offer(walletData)
            awaitClose {}
        }

    fun checkWalletFromList(mintAddress: String): Result<ConstWallet> {
        return if (sendCoinWalletList.isNotEmpty()) {
            if (mintAddress == Constants.OWNER_SOL) {
                return Result.Success(WalletDataConst.getWalletConstList()[0])
            }
            val findFromAll =
                WalletDataConst.getWalletConstList().find { walletItem -> walletItem.mint == mintAddress }
            if (findFromAll != null) {
                Result.Success(findFromAll)
            } else
                Result.Error(CallException(ERROR_NULL_DATA, "Invalid QR code"))
        } else {
            Result.Error(CallException(Constants.ERROR_EMPTY_ALL_WALLETS, "You don't have any wallets"))
        }
    }

    suspend fun addCoin(addCoinItem: AddCoinItem): Result<Boolean> {
//        val secretKey = preferenceService.getActiveWallet()?.secretKey
        val secretKey = "preferenceService.getActiveWallet()?.secretKey"
        val payer = Account(Base58.decode(secretKey))
        val mintAddress = PublicKey(addCoinItem.mintAddress)
        val newAccount = Account()
        addCoinItem.walletAddress = newAccount.publicKey.toBase58()
//        val activeWallet = preferenceService.getActiveWallet()
//        val fromPublicKey = activeWallet?.publicKey!!
        val fromPublicKey = "activeWallet?.publicKey!!"
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
                )?.transferInfoToActivityItem(fromPublicKey, "", "", "", true)
                return Result.Success(true)
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
        walletData.clear()
        sendCoinWalletList.clear()
        pirChatList.clear()
        yourWalletBalance = 0.0

        walletData.addAll(walletDataTemp)
        coroutineScope {
            walletData.map { walletsItem ->
                async {
                    when (
                        val overbookData =
                            wowletApiCallRepository.getOrderBooks(walletsItem.tokenSymbol + "USDT")
                    ) {
                        is Result.Success -> {
                            overbookData.data?.let {
                                if (it.bids.isNotEmpty()) {
                                    val price = it.bids[0].price
//                                    walletsItem.walletBinds = price
//                                    walletsItem.price = price * walletsItem.amount
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