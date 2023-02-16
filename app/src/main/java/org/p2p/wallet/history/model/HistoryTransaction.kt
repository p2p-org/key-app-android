package org.p2p.wallet.history.model

import org.threeten.bp.ZonedDateTime

abstract class HistoryTransaction {
    abstract fun getHistoryTransactionId(): String
    abstract val date: ZonedDateTime
}
