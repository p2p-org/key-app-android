package org.p2p.wallet.history.model

import com.github.mikephil.charting.data.Entry
import org.p2p.wallet.main.model.Token
import org.threeten.bp.ZonedDateTime

sealed class HistoryItem {
    data class Header(val token: Token.Active, val sol: Token.Active, val entries: List<Entry>) : HistoryItem()
    data class TransactionItem(val transaction: HistoryTransaction) : HistoryItem()
    object Empty : HistoryItem()
    data class DateItem(val date: ZonedDateTime) : HistoryItem()
}