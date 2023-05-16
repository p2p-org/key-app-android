package org.p2p.wallet.sell.interactor

import android.content.res.Resources
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import org.p2p.core.utils.Constants
import org.p2p.core.utils.formatFiat
import org.p2p.core.utils.formatToken
import org.p2p.ethereumkit.external.model.ERC20Tokens
import org.p2p.wallet.R
import org.p2p.wallet.common.date.isSameDayAs
import org.p2p.wallet.common.date.toZonedDateTime
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.model.bridge.BridgeHistoryTransaction
import org.p2p.wallet.history.model.rpc.RpcHistoryTransaction
import org.p2p.wallet.history.ui.model.HistoryItem
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.moonpay.model.SellTransaction
import org.p2p.wallet.moonpay.serversideapi.response.SellTransactionStatus
import org.p2p.wallet.sell.ui.lock.SellTransactionViewDetails
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.cutStart
import org.p2p.wallet.utils.getStatusIcon

private const val USDT_ETH_TOKEN_SYMBOL = "USDTet"
private const val USDC_ETH_TOKEN_SYMBOL = "USDCet"

class HistoryItemMapper(
    private val resources: Resources,
    private val dispatchers: CoroutineDispatchers,
    private val userLocalRepository: UserLocalRepository
) {

    private val historyItemFlow = MutableStateFlow<List<HistoryItem>?>(null)

    fun getHistoryAdapterItemFlow(): MutableStateFlow<List<HistoryItem>?> {
        return historyItemFlow
    }

    suspend fun toAdapterItem(
        tokenMintAddress: Base58String?,
        transactions: List<HistoryTransaction>,
        userSendLinksCount: Int
    ) {
        val rpcHistoryItems = mutableListOf<HistoryItem>()
        val sellHistoryItems = mutableListOf<HistoryItem>()
        val bridgeHistoryItems = mutableListOf<HistoryItem>()
        val filterBundleIds = transactions.filterIsInstance<BridgeHistoryTransaction>()
            .filter { it.isProcessing() }
            .map { it.getHistoryTransactionId() }
        withContext(dispatchers.io) {
            transactions.forEachIndexed { _, item ->
                when (item) {
                    is RpcHistoryTransaction -> {
                        if (item.isNotProcessing(filterBundleIds)) {
                            parse(item, rpcHistoryItems)
                        }
                    }
                    is SellTransaction -> {
                        // Sell transactions with cancel reason, should not appear in history
                        if (!item.isCancelled()) {
                            parse(item, sellHistoryItems)
                        }
                    }
                    is BridgeHistoryTransaction -> {
                        parse(item, bridgeHistoryItems)
                    }
                }
            }

            val swapBannerItem: HistoryItem.SwapBannerItem? = tokenMintAddress?.let(::createSwapBanner)
            val userSendLinksItem: HistoryItem.UserSendLinksItem? = createUserSendLinksItem(userSendLinksCount)

            val historyItems = listOfNotNull(swapBannerItem, userSendLinksItem)
                .plus(sellHistoryItems)
                .plus(bridgeHistoryItems)
                .plus(rpcHistoryItems)
            historyItemFlow.emit(historyItems)
        }
    }

    private fun RpcHistoryTransaction.isNotProcessing(filterBundleIds: List<String>): Boolean {
        return when (this) {
            is RpcHistoryTransaction.WormholeReceive -> claimKey !in filterBundleIds
            is RpcHistoryTransaction.WormholeSend -> message !in filterBundleIds
            else -> true
        }
    }

    private fun createSwapBanner(tokenMintAddress: Base58String): HistoryItem.SwapBannerItem? {
        return when (tokenMintAddress.base58Value) {
            Constants.USDC_MINT -> {
                HistoryItem.SwapBannerItem(
                    sourceTokenMintAddress = Constants.USDC_MINT,
                    sourceTokenSymbol = Constants.USDC_SYMBOL,
                    destinationTokenMintAddress = ERC20Tokens.USDC.mintAddress,
                    destinationTokenSymbol = USDC_ETH_TOKEN_SYMBOL
                )
            }
            Constants.USDT_MINT -> {
                HistoryItem.SwapBannerItem(
                    sourceTokenMintAddress = Constants.USDT_MINT,
                    sourceTokenSymbol = Constants.USDT_SYMBOL,
                    destinationTokenMintAddress = ERC20Tokens.USDT.mintAddress,
                    destinationTokenSymbol = USDT_ETH_TOKEN_SYMBOL
                )
            }
            else -> {
                null
            }
        }
    }

    private fun createUserSendLinksItem(userSendLinksCount: Int): HistoryItem.UserSendLinksItem? {
        return HistoryItem.UserSendLinksItem(userSendLinksCount)
            .takeIf { userSendLinksCount > 0 }
    }

    fun parse(transaction: RpcHistoryTransaction, cache: MutableList<HistoryItem>) {
        val isCurrentAndPreviousTransactionOnSameDay =
            cache.isNotEmpty() && cache.last().date.isSameDayAs(transaction.date)
        var tokenIconUrl: String? = null
        var sourceTokenIconUrl: String? = null
        var destinationTokenIconUrl: String? = null

        val startTitle: String?
        val startSubtitle: String?
        var endTopValue: String? = null
        var endTopValueTextColor: Int? = null
        var endBottomValue: String? = null

        val iconRes: Int
        when (transaction) {
            is RpcHistoryTransaction.Swap -> with(transaction) {
                sourceTokenIconUrl = sourceIconUrl
                destinationTokenIconUrl = destinationIconUrl

                iconRes = R.drawable.ic_swap_arrows
                startTitle = "$sourceSymbol to $destinationSymbol"
                startSubtitle = resources.getString(getTypeName())
                endTopValue = "+${getDestinationTotal()}"
                endTopValueTextColor = getTextColor()
                endBottomValue = getSourceTotal()
            }
            is RpcHistoryTransaction.Transfer -> with(transaction) {
                tokenIconUrl = getTokenIconUrl()
                iconRes = getIcon()

                startTitle = getFormattedUsernameOrAddress()
                startSubtitle = resources.getString(getTypeName())
                endTopValue = getFormattedFiatValue()
                endTopValueTextColor = getTextColor()
                endBottomValue = getTotalWithSymbol()
            }
            is RpcHistoryTransaction.StakeUnstake -> with(transaction) {
                tokenIconUrl = getTokenIconUrl()
                iconRes = getIcon()

                startTitle = resources.getString(getTypeName())
                startSubtitle = resources.getString(R.string.transaction_history_vote_format, getAddress())
                endTopValue = getValue()
                endTopValueTextColor = getTextColor()
                endBottomValue = getTotal()
            }
            is RpcHistoryTransaction.BurnOrMint -> with(transaction) {
                tokenIconUrl = iconUrl
                iconRes = R.drawable.ic_placeholder_image

                startTitle = resources.getString(getTitle())
                startSubtitle =
                    resources.getString(R.string.transaction_history_signature_format, signature.cutStart())
                endTopValue = getUsdAmount()
                endTopValueTextColor = getTextColor()
                endBottomValue = getTotal()
            }
            is RpcHistoryTransaction.CreateAccount -> with(transaction) {
                tokenIconUrl = iconUrl
                iconRes = R.drawable.ic_transaction_create

                startTitle = resources.getString(R.string.transaction_history_create)
                startSubtitle =
                    resources.getString(R.string.transaction_history_signature_format, signature.cutStart())
            }
            is RpcHistoryTransaction.CloseAccount -> with(transaction) {
                tokenIconUrl = iconUrl
                iconRes = R.drawable.ic_transaction_closed

                startTitle = resources.getString(R.string.transaction_history_closed)
                startSubtitle =
                    resources.getString(R.string.transaction_history_signature_format, signature.cutStart())
            }
            is RpcHistoryTransaction.WormholeSend -> with(transaction) {
                tokenIconUrl = iconUrl
                iconRes = R.drawable.ic_transaction_send
                startTitle = resources.getString(getTitle())
                startSubtitle = resources.getString(getSubtitle())
                endTopValue = getUsdAmount()
                endTopValueTextColor = getTextColor()
                endBottomValue = getTotal()
            }

            is RpcHistoryTransaction.WormholeReceive -> with(transaction) {
                tokenIconUrl = iconUrl
                iconRes = R.drawable.ic_transaction_send
                startTitle = resources.getString(getTitle())
                startSubtitle = resources.getString(getSubtitle())
                endTopValue = getUsdAmount()
                endTopValueTextColor = getTextColor()
                endBottomValue = getTotal()
            }
            is RpcHistoryTransaction.Unknown -> {
                iconRes = R.drawable.ic_transaction_unknown

                startTitle = resources.getString(R.string.transaction_history_unknown)
                startSubtitle = resources.getString(
                    R.string.transaction_history_signature_format,
                    transaction.signature.cutStart()
                )
            }
        }
        val historyItem = HistoryItem.TransactionItem(
            transactionId = transaction.getHistoryTransactionId(),
            sourceIconUrl = sourceTokenIconUrl,
            destinationIconUrl = destinationTokenIconUrl,
            tokenIconUrl = tokenIconUrl,
            iconRes = iconRes,
            startTitle = startTitle,
            startSubtitle = startSubtitle,
            endTopValue = endTopValue,
            endTopValueTextColor = endTopValueTextColor,
            endBottomValue = endBottomValue,
            statusIcon = transaction.status.getStatusIcon(),
            date = transaction.date
        )
        if (isCurrentAndPreviousTransactionOnSameDay) {
            cache.add(historyItem)
        } else {
            cache.addAll(
                listOf(
                    HistoryItem.DateItem(transaction.date),
                    historyItem
                )
            )
        }
    }

    fun parse(
        transaction: SellTransaction,
        cache: MutableList<HistoryItem>
    ) {
        val receiverAddress = if (transaction is SellTransaction.WaitingForDepositTransaction) {
            transaction.moonpayDepositWalletAddress.base58Value
        } else {
            resources.getString(R.string.sell_details_receiver_moonpay_bank)
        }

        val formattedSolAmount = transaction.amounts.tokenAmount.formatToken()
        val formattedFiatAmount = transaction.amounts.amountInFiat.formatFiat()
        val fiatUiName = transaction.selectedFiat.uiSymbol

        val iconRes: Int
        val backgroundRes: Int
        val iconColor: Int
        val titleStatus: String
        val subtitleReceiver: String

        var endTopValue: String = resources.getString(
            R.string.transaction_history_moonpay_amount_sol,
            formattedSolAmount,
        )
        when (transaction.status) {
            SellTransactionStatus.WAITING_FOR_DEPOSIT -> {
                titleStatus = resources.getString(R.string.transaction_history_moonpay_waiting_for_deposit_title)
                subtitleReceiver = resources.getString(
                    R.string.transaction_history_moonpay_waiting_for_deposit_subtitle,
                    receiverAddress.cutStart()
                )
                iconRes = R.drawable.ic_alert_rounded
                backgroundRes = R.drawable.bg_rounded_solid_rain_24
                iconColor = R.color.icons_night
            }
            SellTransactionStatus.FAILED -> {
                titleStatus = resources.getString(R.string.transaction_history_moonpay_failed_title)
                subtitleReceiver = resources.getString(R.string.transaction_history_moonpay_failed_subtitle)
                iconRes = R.drawable.ic_alert_rounded
                backgroundRes = R.drawable.bg_rounded_solid_rose20_24
                iconColor = R.color.icons_rose
            }
            SellTransactionStatus.PENDING -> {
                titleStatus = resources.getString(R.string.transaction_history_moonpay_pending_title)
                subtitleReceiver = resources.getString(R.string.transaction_history_moonpay_completed_subtitle)
                iconRes = R.drawable.ic_action_schedule_filled
                backgroundRes = R.drawable.bg_rounded_solid_rain_24
                iconColor = R.color.icons_night
            }
            SellTransactionStatus.COMPLETED -> {
                titleStatus = resources.getString(R.string.transaction_history_moonpay_completed_title)
                subtitleReceiver = resources.getString(R.string.transaction_history_moonpay_completed_subtitle)
                iconRes = R.drawable.ic_action_schedule_filled
                backgroundRes = R.drawable.bg_rounded_solid_rain_24
                iconColor = R.color.icons_night

                endTopValue = resources.getString(
                    R.string.transaction_history_moonpay_amount_fiat,
                    formattedFiatAmount,
                    fiatUiName.uppercase()
                )
            }
        }
        cache.add(
            HistoryItem.MoonpayTransactionItem(
                transactionId = transaction.transactionId,
                statusIconRes = iconRes,
                statusBackgroundRes = backgroundRes,
                statusIconColor = iconColor,
                titleStatus = titleStatus,
                subtitleReceiver = subtitleReceiver,
                endTopValue = endTopValue,
                date = transaction.updatedAt.toZonedDateTime()
            )
        )
    }

    fun parse(item: BridgeHistoryTransaction, cache: MutableList<HistoryItem>) {
        if (item is BridgeHistoryTransaction.Send) {
            val sendTokenSymbol = item.sendDetails.amount.symbol
            val tokenIconUrl = userLocalRepository.getTokensData()
                .firstOrNull { it.symbol == sendTokenSymbol }
                ?.iconUrl

            val item = HistoryItem.BridgeSendItem(
                id = item.id,
                sendDetails = item.sendDetails,
                tokenIconUrl = tokenIconUrl
            )
            cache.add(item)
        } else if (item is BridgeHistoryTransaction.Claim) {
            val claimTokenSymbol = item.bundle.resultAmount.symbol
            val tokenIconUrl = userLocalRepository.getTokensData()
                .firstOrNull { it.symbol == claimTokenSymbol }
                ?.iconUrl

            val item = HistoryItem.BridgeClaimItem(
                bundleId = item.bundleId,
                bundle = item.bundle,
                tokenIconUrl = tokenIconUrl
            )
            cache.add(item)
        }
    }

    fun toSellDetailsModel(sellTransaction: SellTransaction): SellTransactionViewDetails {
        val receiverAddress = if (sellTransaction is SellTransaction.WaitingForDepositTransaction) {
            sellTransaction.moonpayDepositWalletAddress.base58Value
        } else {
            resources.getString(R.string.sell_details_receiver_moonpay_bank)
        }

        val formattedSolAmount = sellTransaction.amounts.tokenAmount.formatToken()
        val formattedFiatAmount = sellTransaction.amounts.amountInFiat.formatFiat()
        val fiatUiName = sellTransaction.selectedFiat.uiSymbol

        return SellTransactionViewDetails(
            transactionId = sellTransaction.transactionId,
            status = sellTransaction.status,
            formattedSolAmount = formattedSolAmount,
            formattedFiatAmount = formattedFiatAmount,
            fiatUiName = fiatUiName,
            receiverAddress = receiverAddress,
            updatedAt = sellTransaction.updatedAt,
        )
    }
}
