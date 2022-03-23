package org.p2p.wallet.feerelayer.api

import com.google.gson.annotations.SerializedName

data class RequestAccountMeta(
    @SerializedName("pubkey")
    val pubkeyIndex: Int,
    @SerializedName("is_signer")
    val isSigner: Boolean,
    @SerializedName("is_writable")
    val isWritable: Boolean
)
