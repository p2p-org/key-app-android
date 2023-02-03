package org.p2p.wallet.history.model

import org.p2p.uikit.utils.recycler.RoundedItem
import org.p2p.wallet.moonpay.serversideapi.response.SellTransactionStatus
import org.p2p.wallet.sell.ui.lock.SellTransactionViewDetails
import org.p2p.wallet.utils.emptyString
import org.threeten.bp.ZonedDateTime

sealed class HistoryItem : RoundedItem {
    data class TransactionItem(val transaction: HistoryTransaction) : HistoryItem() {
        override fun needDecorate(): Boolean = true

        override fun roundingHash(): String = HistoryItem::javaClass.name
    }

    data class DateItem(val date: ZonedDateTime) : HistoryItem()

    data class MoonpayTransactionItem(val transactionDetails: SellTransactionViewDetails) : HistoryItem() {
        val status: SellTransactionStatus = transactionDetails.status
        val amountInSol: String = transactionDetails.formattedSolAmount
        val amountInFiat: String = transactionDetails.formattedFiatAmount
        override fun needDecorate(): Boolean = true

        override fun roundingHash(): String = HistoryItem::javaClass.name
    }

    override fun needDecorate(): Boolean = false
    override fun roundingHash(): String = emptyString()
}
