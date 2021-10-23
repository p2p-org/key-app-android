package org.p2p.wallet.rpc.api

import com.google.gson.annotations.SerializedName
import java.math.BigInteger

data class FeeSplTransferRequest(
    @SerializedName("sender_token_account_pubkey")
    val senderTokenAccountPubkey: String,
    @SerializedName("recipient_pubkey")
    val recipientPubkey: String,
    @SerializedName("token_mint_pubkey")
    val tokenMintPubkey: String,
    @SerializedName("authority_pubkey")
    val authorityPubkey: String,
    @SerializedName("amount")
    val lamports: BigInteger,
    @SerializedName("decimals")
    val decimals: Int,
    @SerializedName("signature")
    val signature: String,
    @SerializedName("blockhash")
    val blockhash: String
)