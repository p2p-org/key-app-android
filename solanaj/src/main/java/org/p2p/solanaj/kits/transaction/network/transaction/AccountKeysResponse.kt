package org.p2p.solanaj.kits.transaction.network.transaction

import com.google.gson.annotations.SerializedName

data class AccountKeysResponse(
    @SerializedName("pubkey")
    val publicKey: String?,

    @SerializedName("signer")
    private val isSigner: Boolean,

    @SerializedName("writable")
    private val isWritable: Boolean
)
