package com.p2p.wallet.token.model

import com.p2p.wallet.amount.scaleAmount
import org.threeten.bp.ZonedDateTime
import java.math.BigDecimal

sealed class Transaction(
    open val date: ZonedDateTime
) {

    abstract val transactionId: String
    abstract val tokenSymbol: String

    data class Swap(
        override val transactionId: String,
        override val date: ZonedDateTime,
        override val tokenSymbol: String,
        val amountA: BigDecimal,
        val amountB: BigDecimal,
        val mintA: String,
        val mintB: String
    ) : Transaction(date) {

        fun getFormattedAmount(): String = "${amountA.scaleAmount()} $"

        fun getFormattedTotal(): String = "${amountA.scaleAmount()} $tokenSymbol"
    }

    data class Send(
        override val transactionId: String,
        override val date: ZonedDateTime,
        override val tokenSymbol: String,
        val amount: BigDecimal,
        val total: BigDecimal,
        val destination: String
    ) : Transaction(date) {

        fun getFormattedAmount(): String = "${amount.scaleAmount()} $"

        fun getFormattedTotal(): String = "${total.scaleAmount()} $tokenSymbol"
    }

    data class Receive(
        override val transactionId: String,
        override val date: ZonedDateTime,
        override val tokenSymbol: String,
        val amount: BigDecimal,
        val total: BigDecimal,
        val senderAddress: String
    ) : Transaction(date) {

        fun getFormattedAmount(): String = "${amount.scaleAmount()} $"

        fun getFormattedTotal(): String = "${total.scaleAmount()} $tokenSymbol"
    }

    data class CloseAccount(
        val account: String,
        val destination: String,
        val owner: String,
        override val transactionId: String,
        override val date: ZonedDateTime,
        override val tokenSymbol: String
    ) : Transaction(date)

    data class Unknown(
        override val transactionId: String,
        override val date: ZonedDateTime,
        override val tokenSymbol: String
    ) : Transaction(date)
}