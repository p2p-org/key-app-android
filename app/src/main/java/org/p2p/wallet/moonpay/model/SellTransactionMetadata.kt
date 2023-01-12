package org.p2p.wallet.moonpay.model

data class SellTransactionMetadata(
    val createdAt: String,
    val updatedAt: String,
    val accountId: String,
    val customerId: String,
    val bankAccountId: String,
    val externalTransactionId: String?,
    val externalCustomerId: String?,
    val countryAbbreviation: String,
    val stateAbbreviation: String?
)
