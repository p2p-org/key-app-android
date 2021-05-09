package com.p2p.wallet.token.model

import org.threeten.bp.ZonedDateTime
import java.math.BigDecimal
import java.math.RoundingMode

sealed class Transaction(
    open val status: Status,
    open val date: ZonedDateTime
) {

    abstract val transactionId: String
    abstract val tokenSymbol: String
    abstract val amount: BigDecimal
    abstract val total: BigDecimal

    fun getFormattedAmount(): String = "${amount.setScale(2, RoundingMode.HALF_EVEN)} $"

    fun getFormattedTotal(): String = "${total.setScale(6, RoundingMode.HALF_EVEN)} $tokenSymbol"

    data class Swap(
        override val transactionId: String,
        override val amount: BigDecimal,
        override val total: BigDecimal,
        override val status: Status,
        override val date: ZonedDateTime,
        override val tokenSymbol: String,
        val sourceTokenUrl: String,
        val destinationTokenUrl: String
    ) : Transaction(status, date)

    data class Send(
        override val transactionId: String,
        override val amount: BigDecimal,
        override val total: BigDecimal,
        override val status: Status,
        override val date: ZonedDateTime,
        override val tokenSymbol: String,
        val destination: String
    ) : Transaction(status, date)

    data class Receive(
        override val transactionId: String,
        override val amount: BigDecimal,
        override val total: BigDecimal,
        override val status: Status,
        override val date: ZonedDateTime,
        override val tokenSymbol: String,
        val senderAddress: String
    ) : Transaction(status, date)
}

enum class Status {
    SUCCESS, PENDING, ERROR;
}