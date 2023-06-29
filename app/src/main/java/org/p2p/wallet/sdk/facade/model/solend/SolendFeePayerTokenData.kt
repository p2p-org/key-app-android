package org.p2p.wallet.sdk.facade.model.solend

import com.google.gson.annotations.SerializedName
import org.p2p.core.crypto.Base58String

data class SolendFeePayerTokenData(
    @SerializedName("sender_account_pubkey")
    val senderAccountAddress: Base58String,
    @SerializedName("recipient_account_pubkey")
    val recipientAccountAddress: Base58String,
    @SerializedName("mint_pubkey")
    val tokenMintAddress: Base58String,
    @SerializedName("authority_pubkey")
    val transferAuthorityAddress: Base58String,
    @SerializedName("exchange_rate")
    val tokenExchangeRateLamports: Double,
    @SerializedName("decimals")
    val tokenDecimals: Int,
)
