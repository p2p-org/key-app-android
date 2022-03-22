package org.p2p.wallet.feerelayer.api

import com.google.gson.annotations.SerializedName

class SwapTransactionSignaturesRequest(
    @SerializedName("user_authority_signature")
    val userAuthoritySignature: String,
    @SerializedName("transfer_authority_signature")
    val transferAuthoritySignature: String?
)
