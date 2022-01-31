package org.p2p.wallet.feerelayer.api

import com.google.gson.annotations.SerializedName
import java.math.BigInteger

class RelayTransferRequest(
    @SerializedName("sender_token_account_pubkey")
    val senderTokenAccountPubkey: String,
    @SerializedName("recipient_pubkey")
    val recipientPubkey: String,
    @SerializedName("token_mint_pubkey")
    val tokenMintPubkey: String,
    @SerializedName("authority_pubkey")
    val authorityPubkey: String,
    @SerializedName("amount")
    val amount: BigInteger,
    @SerializedName("decimals")
    val decimals: Int,
    @SerializedName("fee_amount")
    val feeAmount: BigInteger,
    @SerializedName("authority_signature")
    val authoritySignature: String,
    @SerializedName("blockhash")
    val blockhash: String
)