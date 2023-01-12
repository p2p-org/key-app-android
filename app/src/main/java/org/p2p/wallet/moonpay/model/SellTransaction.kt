package org.p2p.wallet.moonpay.model

import org.p2p.wallet.moonpay.repository.sell.SellTransactionFiatCurrency
import org.p2p.wallet.moonpay.serversideapi.response.SellTransactionFailureReason
import org.p2p.wallet.moonpay.serversideapi.response.SellTransactionStatus
import org.p2p.wallet.utils.Base58String
import java.math.BigDecimal

sealed class SellTransaction(
    val status: SellTransactionStatus
) {
    abstract val metadata: SellTransactionMetadata
    abstract val transactionId: String
    abstract val amounts: SellTransactionAmounts
    abstract val userAddress: Base58String
    abstract val selectedFiat: SellTransactionFiatCurrency

    fun getFiatAmount(): BigDecimal = when (selectedFiat) {
        SellTransactionFiatCurrency.EUR -> amounts.eurAmount
        SellTransactionFiatCurrency.USD -> amounts.usdAmount
        SellTransactionFiatCurrency.GBP -> amounts.gbpAmount
    }

    data class WaitingForDepositTransaction(
        override val metadata: SellTransactionMetadata,
        override val transactionId: String,
        override val amounts: SellTransactionAmounts,
        override val userAddress: Base58String,
        override val selectedFiat: SellTransactionFiatCurrency,
        val moonpayDepositWalletAddress: Base58String
    ) : SellTransaction(SellTransactionStatus.WAITING_FOR_DEPOSIT)

    data class PendingTransaction(
        override val metadata: SellTransactionMetadata,
        override val transactionId: String,
        override val amounts: SellTransactionAmounts,
        override val selectedFiat: SellTransactionFiatCurrency,
        override val userAddress: Base58String,
    ) : SellTransaction(SellTransactionStatus.PENDING)

    data class CompletedTransaction(
        override val metadata: SellTransactionMetadata,
        override val transactionId: String,
        override val amounts: SellTransactionAmounts,
        override val selectedFiat: SellTransactionFiatCurrency,
        override val userAddress: Base58String,
    ) : SellTransaction(SellTransactionStatus.COMPLETED)

    data class FailedTransaction(
        override val metadata: SellTransactionMetadata,
        override val transactionId: String,
        override val amounts: SellTransactionAmounts,
        override val selectedFiat: SellTransactionFiatCurrency,
        override val userAddress: Base58String,
        val failureReason: SellTransactionFailureReason?,
    ) : SellTransaction(SellTransactionStatus.FAILED)
}

fun SellTransaction.isCancelled(): Boolean {
    return if (this is SellTransaction.FailedTransaction) {
        this.failureReason != SellTransactionFailureReason.CANCELLED
    } else {
        true
    }
}
