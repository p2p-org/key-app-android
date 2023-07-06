package org.p2p.wallet.auth.gateway.api.response

import com.google.gson.annotations.SerializedName
import org.p2p.core.crypto.Base58String

data class ConfirmRestoreWalletResponse(
    @SerializedName("solana_pubkey")
    val restoredUserPublicKey: Base58String,

    @SerializedName("ethereum_id")
    val ethereumId: String,

    @SerializedName("share")
    val thirdShareStructBase64: String,

    @SerializedName("payload")
    val encryptedMnemonicsStructBase64: String,
)
