package com.p2p.wallet.token.model

import org.threeten.bp.ZonedDateTime

sealed class TransactionOrDateItem {
    data class TransactionItem(val transaction: Transaction) : TransactionOrDateItem()
    data class DateItem(val date: ZonedDateTime) : TransactionOrDateItem()
}