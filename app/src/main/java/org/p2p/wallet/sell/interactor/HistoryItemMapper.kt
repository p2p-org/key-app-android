package org.p2p.wallet.sell.interactor

import android.content.res.Resources
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.formatUsd
import org.p2p.wallet.R
import org.p2p.wallet.common.date.isSameDayAs
import org.p2p.wallet.history.model.HistoryItem
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.moonpay.model.SellTransaction
import org.p2p.wallet.sell.ui.lock.SellTransactionViewDetails

class HistoryItemMapper(private val resources: Resources) {
    fun fromDomainBlockchain(
        transactions: List<HistoryTransaction>
    ): List<HistoryItem> = transactions.flatMapIndexed { i, transaction ->
        val isCurrentAndPreviousTransactionOnSameDay =
            i > 0 && transactions[i - 1].date.isSameDayAs(transaction.date)
        if (isCurrentAndPreviousTransactionOnSameDay) {
            listOf(HistoryItem.TransactionItem(transaction))
        } else {
            listOf(
                HistoryItem.DateItem(transaction.date),
                // todo map items according to state
                HistoryItem.TransactionItem(transaction)
            )
        }
    }

    fun fromDomainSell(
        transactions: List<SellTransaction>
    ): List<HistoryItem.MoonpayTransactionItem> = transactions.map {
        val receiverAddress = if (it is SellTransaction.WaitingForDepositTransaction) {
            it.moonpayDepositWalletAddress.base58Value
        } else {
            resources.getString(R.string.you_bank_account_via_moonpay)
        }
        SellTransactionViewDetails(
            transactionId = it.transactionId,
            status = it.status,
            formattedSolAmount = it.amounts.tokenAmount.formatToken(),
            formattedUsdAmount = it.getFiatAmount().formatUsd(),
            receiverAddress = receiverAddress
        )
            .let(HistoryItem::MoonpayTransactionItem)
    }
}
