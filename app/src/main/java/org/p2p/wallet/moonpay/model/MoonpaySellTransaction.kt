package org.p2p.wallet.moonpay.model

import org.p2p.wallet.utils.Base58String

data class MoonpaySellTransaction(
    val transactionId: String,
    val createdAt: String,
    val updatedAt: String,
    val status: SellTransactionStatus,
    val amounts: SellTransactionAmounts,
    val accountId: String,
    val customerId: String,
    val bankAccountId: String,
    val externalTransactionId: String?,
    val externalCustomerId: String?,
    val countryAbbreviation: String,
    val stateAbbreviation: String?,
    val userAddress: Base58String
) {
    enum class SellTransactionStatus(val jsonValue: String) {
        WAITING_FOR_DEPOSIT("waitingForDeposit"),
        PENDING("pending"),
        FAILED("failed"),
        COMPLETED("completed"),
        UNKNOWN("");

        companion object {
            fun fromString(value: String): SellTransactionStatus = values().find { it.jsonValue == value } ?: UNKNOWN
        }
    }
}
