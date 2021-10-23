package org.p2p.wallet.rpc.api

import com.google.gson.annotations.SerializedName
import java.math.BigInteger

data class FeeSolTransferRequest(
    @SerializedName("sender_pubkey")
    val senderPubkey: String,
    @SerializedName("recipient_pubkey")
    val recipientPubkey: String,
    @SerializedName("lamports")
    val lamports: BigInteger,
    @SerializedName("signature")
    val signature: String,
    @SerializedName("blockhash")
    val blockhash: String,
)