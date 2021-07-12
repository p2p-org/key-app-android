package com.p2p.wallet.token.model

import com.p2p.wallet.utils.scaleAmount
import org.threeten.bp.ZonedDateTime
import java.math.BigDecimal

sealed class Transaction(
    open val date: ZonedDateTime
) {

    abstract val signature: String

    data class Swap(
        override val signature: String,
        override val date: ZonedDateTime,
        val amountA: BigDecimal,
        val amountB: BigDecimal,
        val amountReceivedInUsd: BigDecimal,
        val sourceSymbol: String,
        val sourceTokenUrl: String,
        val destinationSymbol: String,
        val destinationTokenUrl: String
    ) : Transaction(date)

    data class Send(
        override val signature: String,
        override val date: ZonedDateTime,
        val tokenSymbol: String,
        val amount: BigDecimal,
        val total: BigDecimal,
        val destination: String
    ) : Transaction(date) {

        fun getFormattedAmount(): String = "${amount.scaleAmount()} $"

        fun getFormattedTotal(): String = "${total.scaleAmount()} $tokenSymbol"
    }

    data class Receive(
        override val signature: String,
        override val date: ZonedDateTime,
        val tokenSymbol: String,
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
        val tokenSymbol: String,
        override val signature: String,
        override val date: ZonedDateTime
    ) : Transaction(date)

    data class Unknown(
        override val signature: String,
        override val date: ZonedDateTime
    ) : Transaction(date)
}