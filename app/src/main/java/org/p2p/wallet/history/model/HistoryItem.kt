package org.p2p.wallet.history.model

import org.threeten.bp.ZonedDateTime
import org.p2p.uikit.utils.recycler.RoundedItem

sealed interface HistoryItem {
    data class TransactionItem(val transaction: HistoryTransaction) : HistoryItem, RoundedItem

    data class DateItem(val date: ZonedDateTime) : HistoryItem

    data class MoonpayTransactionItem(
        val transactionId: String,

        val statusIconRes: Int,
        val statusBackgroundRes: Int,
        val statusIconColor: Int,

        val titleStatus: String,
        val subtitleReceiver: String,

        val endTopValue: String,
        val endBottomValue: String? = null,
    ) : HistoryItem, RoundedItem
}
