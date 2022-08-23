package org.p2p.wallet.auth.model

import com.google.gson.annotations.SerializedName

data class Web3AuthSignInResponse(
    @SerializedName("ethPublic")
    val ethereumPublicKey: String,
    // bip39 mnemonic
    @SerializedName("privateSOL")
    val mnemonicPhrase: String,
)
