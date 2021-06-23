package com.p2p.wallet.token.model

import com.p2p.wallet.amount.scaleAmount
import org.threeten.bp.ZonedDateTime
import java.math.BigDecimal

sealed class Transaction(
    open val date: ZonedDateTime
) {

    abstract val transactionId: String
    abstract val tokenSymbol: String
    abstract val amount: BigDecimal
    abstract val total: BigDecimal

    fun getFormattedAmount(): String = "${amount.scaleAmount()} $"

    fun getFormattedTotal(): String = "${total.scaleAmount()} $tokenSymbol"

    data class Swap(
        override val transactionId: String,
        override val amount: BigDecimal,
        override val total: BigDecimal,
        override val date: ZonedDateTime,
        override val tokenSymbol: String,
        val sourceTokenUrl: String,
        val destinationTokenUrl: String
    ) : Transaction(date)

    data class Send(
        override val transactionId: String,
        override val amount: BigDecimal,
        override val total: BigDecimal,
        override val date: ZonedDateTime,
        override val tokenSymbol: String,
        val destination: String
    ) : Transaction(date)

    data class Receive(
        override val transactionId: String,
        override val amount: BigDecimal,
        override val total: BigDecimal,
        override val date: ZonedDateTime,
        override val tokenSymbol: String,
        val senderAddress: String
    ) : Transaction(date)

    data class CloseAccount(
        val account: String,
        val destination: String,
        val owner: String,
        override val transactionId: String,
        override val amount: BigDecimal,
        override val total: BigDecimal,
        override val date: ZonedDateTime,
        override val tokenSymbol: String
    ) : Transaction(date)

    data class Unknown(
        override val transactionId: String,
        override val amount: BigDecimal,
        override val total: BigDecimal,
        override val date: ZonedDateTime,
        override val tokenSymbol: String
    ) : Transaction(date)
}