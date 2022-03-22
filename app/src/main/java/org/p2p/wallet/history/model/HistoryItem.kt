package org.p2p.wallet.history.model

import org.threeten.bp.ZonedDateTime

sealed class HistoryItem {
    data class TransactionItem(val transaction: HistoryTransaction) : HistoryItem()
    object Empty : HistoryItem()
    data class DateItem(val date: ZonedDateTime) : HistoryItem()
}
