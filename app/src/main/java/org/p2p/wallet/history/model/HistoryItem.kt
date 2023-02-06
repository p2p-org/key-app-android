package org.p2p.wallet.history.model

import org.p2p.uikit.utils.recycler.RoundedItem
import org.p2p.wallet.moonpay.serversideapi.response.SellTransactionStatus
import org.p2p.wallet.sell.ui.lock.SellTransactionViewDetails
import org.threeten.bp.ZonedDateTime

sealed interface HistoryItem {
    data class TransactionItem(val transaction: HistoryTransaction) : HistoryItem, RoundedItem

    data class DateItem(val date: ZonedDateTime) : HistoryItem

    data class MoonpayTransactionItem(val transactionDetails: SellTransactionViewDetails) : HistoryItem, RoundedItem {
        val status: SellTransactionStatus = transactionDetails.status
        val amountInSol: String = transactionDetails.formattedSolAmount
        val amountInFiat: String = transactionDetails.formattedFiatAmount
    }
}
