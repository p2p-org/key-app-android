package org.p2p.wallet.sell.interactor

import android.content.res.Resources
import org.p2p.core.utils.formatFiat
import org.p2p.core.utils.formatToken
import org.p2p.wallet.R
import org.p2p.wallet.common.date.isSameDayAs
import org.p2p.wallet.history.model.HistoryItem
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.moonpay.model.SellTransaction
import org.p2p.wallet.moonpay.serversideapi.response.SellTransactionStatus
import org.p2p.wallet.sell.ui.lock.SellTransactionViewDetails
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.cutStart
import org.p2p.wallet.utils.getStatusIcon

class HistoryItemMapper(private val resources: Resources) {

    fun fromDomainBlockchain(
        transactions: List<HistoryTransaction>
    ): List<HistoryItem> = transactions.flatMapIndexed { i, transaction ->
        val isCurrentAndPreviousTransactionOnSameDay =
            i > 0 && transactions[i - 1].date.isSameDayAs(transaction.date)
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
            is HistoryTransaction.Swap -> with(transaction) {
                sourceTokenIconUrl = sourceIconUrl
                destinationTokenIconUrl = destinationIconUrl

                iconRes = R.drawable.ic_swap_simple
                startTitle = "$sourceSymbol to $destinationSymbol"
                startSubtitle = resources.getString(getTypeName())
                endTopValue = "+${getDestinationTotal()}"
                endTopValueTextColor = getTextColor()
                endBottomValue = "-${getSourceTotal()}"
            }
            is HistoryTransaction.Transfer -> with(transaction) {
                tokenIconUrl = getTokenIconUrl()
                iconRes = getIcon()

                startTitle = getAddress()
                startSubtitle = resources.getString(getTypeName())
                endTopValue = getValue()
                endTopValueTextColor = getTextColor()
                endBottomValue = getTotal()
            }
            is HistoryTransaction.BurnOrMint -> with(transaction) {
                tokenIconUrl = getTokenIconUrl()
                iconRes = getIcon()

                startTitle = resources.getString(getTitle())
                startSubtitle = signature.cutMiddle()
                endTopValue = getTotal()
                endBottomValue = getValue()
            }
            is HistoryTransaction.CreateAccount -> with(transaction) {
                tokenIconUrl = getTokenIconUrl()
                iconRes = R.drawable.ic_transaction_create

                startTitle = resources.getString(R.string.transaction_history_create)
                startSubtitle = signature.cutMiddle()
            }
            is HistoryTransaction.CloseAccount -> with(transaction) {
                tokenIconUrl = getTokenIconUrl()
                iconRes = R.drawable.ic_transaction_closed

                startTitle = resources.getString(R.string.transaction_history_closed)
                startSubtitle = signature.cutMiddle()
            }
            is HistoryTransaction.Unknown -> {
                iconRes = R.drawable.ic_transaction_unknown

                startTitle = resources.getString(R.string.transaction_history_unknown)
                startSubtitle = transaction.signature.cutMiddle()
            }
        }
        val historyItem = HistoryItem.TransactionItem(
            signature = transaction.signature,
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
        )
        if (isCurrentAndPreviousTransactionOnSameDay) {
            listOf(historyItem)
        } else {
            listOf(
                HistoryItem.DateItem(transaction.date),
                historyItem
            )
        }
    }

    fun fromDomainSell(
        transactions: List<SellTransaction>
    ): List<HistoryItem.MoonpayTransactionItem> = transactions.map {
        val receiverAddress = if (it is SellTransaction.WaitingForDepositTransaction) {
            it.moonpayDepositWalletAddress.base58Value
        } else {
            resources.getString(R.string.sell_details_receiver_moonpay_bank)
        }

        val formattedSolAmount = it.amounts.tokenAmount.formatToken()
        val formattedFiatAmount = it.amounts.amountInFiat.formatFiat()
        val fiatUiName = it.selectedFiat.uiSymbol

        val iconRes: Int
        val backgroundRes: Int
        val iconColor: Int
        val titleStatus: String
        val subtitleReceiver: String

        var endTopValue: String = resources.getString(
            R.string.transaction_history_moonpay_amount_sol,
            formattedSolAmount,
        )
        when (it.status) {
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

        HistoryItem.MoonpayTransactionItem(
            transactionId = it.transactionId,
            statusIconRes = iconRes,
            statusBackgroundRes = backgroundRes,
            statusIconColor = iconColor,
            titleStatus = titleStatus,
            subtitleReceiver = subtitleReceiver,
            endTopValue = endTopValue,
        )
    }

    fun sellTransactionToDetails(sellTransaction: SellTransaction): SellTransactionViewDetails {
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
