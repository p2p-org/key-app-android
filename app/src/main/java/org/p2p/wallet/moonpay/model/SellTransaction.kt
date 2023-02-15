package org.p2p.wallet.moonpay.model

import org.p2p.wallet.common.date.toZonedDateTime
import org.p2p.wallet.history.model.moonpay.MoonPayTransaction
import org.p2p.wallet.moonpay.repository.sell.SellTransactionFiatCurrency
import org.p2p.wallet.moonpay.serversideapi.response.SellTransactionFailureReason
import org.p2p.wallet.moonpay.serversideapi.response.SellTransactionStatus
import org.p2p.wallet.utils.Base58String
import org.threeten.bp.ZonedDateTime

sealed class SellTransaction(
    val status: SellTransactionStatus
) : MoonPayTransaction() {
    abstract val metadata: SellTransactionMetadata
    abstract val transactionId: String
    abstract val amounts: SellTransactionAmounts
    abstract val userAddress: Base58String
    abstract val selectedFiat: SellTransactionFiatCurrency
    abstract val updatedAt: String

    fun isCancelled(): Boolean {
        return this is FailedTransaction && failureReason == SellTransactionFailureReason.CANCELLED
    }

    override val date: ZonedDateTime
        get() = updatedAt.toZonedDateTime()

    override fun getHistoryTransactionId(): String {
        return transactionId
    }

    data class WaitingForDepositTransaction(
        override val metadata: SellTransactionMetadata,
        override val transactionId: String,
        override val amounts: SellTransactionAmounts,
        override val userAddress: Base58String,
        override val selectedFiat: SellTransactionFiatCurrency,
        override val updatedAt: String,
        val moonpayDepositWalletAddress: Base58String,
    ) : SellTransaction(SellTransactionStatus.WAITING_FOR_DEPOSIT)

    data class PendingTransaction(
        override val metadata: SellTransactionMetadata,
        override val transactionId: String,
        override val amounts: SellTransactionAmounts,
        override val selectedFiat: SellTransactionFiatCurrency,
        override val userAddress: Base58String,
        override val updatedAt: String,
    ) : SellTransaction(SellTransactionStatus.PENDING)

    data class CompletedTransaction(
        override val metadata: SellTransactionMetadata,
        override val transactionId: String,
        override val amounts: SellTransactionAmounts,
        override val selectedFiat: SellTransactionFiatCurrency,
        override val userAddress: Base58String,
        override val updatedAt: String,
    ) : SellTransaction(SellTransactionStatus.COMPLETED)

    data class FailedTransaction(
        override val metadata: SellTransactionMetadata,
        override val transactionId: String,
        override val amounts: SellTransactionAmounts,
        override val selectedFiat: SellTransactionFiatCurrency,
        override val userAddress: Base58String,
        val failureReason: SellTransactionFailureReason?,
        override val updatedAt: String,
    ) : SellTransaction(SellTransactionStatus.FAILED)
}
