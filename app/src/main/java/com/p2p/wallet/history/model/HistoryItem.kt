package com.p2p.wallet.history.model

import com.github.mikephil.charting.data.Entry
import com.p2p.wallet.main.model.Token
import org.threeten.bp.ZonedDateTime

sealed class HistoryItem {
    data class Header(val token: Token, val sol: Token, val entries: List<Entry>) : HistoryItem()
    data class TransactionItem(val transaction: TransactionType) : HistoryItem()
    object Empty : HistoryItem()
    data class DateItem(val date: ZonedDateTime) : HistoryItem()
}