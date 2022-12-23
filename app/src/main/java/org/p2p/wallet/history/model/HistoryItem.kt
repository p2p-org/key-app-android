package org.p2p.wallet.history.model

import org.p2p.wallet.moonpay.model.MoonpaySellTransaction
import org.p2p.wallet.sell.ui.lock.SellTransactionDetails
import org.threeten.bp.ZonedDateTime

sealed class HistoryItem {
    data class TransactionItem(val transaction: HistoryTransaction) : HistoryItem()
    object Empty : HistoryItem()
    data class DateItem(val date: ZonedDateTime) : HistoryItem()

    data class MoonpayTransactionItem(val transactionDetails: SellTransactionDetails) : HistoryItem() {
        val status: MoonpaySellTransaction.SellTransactionStatus = transactionDetails.status
        val amountInSol: String = transactionDetails.formattedSolAmount
        val amountInUsd: String = transactionDetails.formattedUsdAmount
    }
}
