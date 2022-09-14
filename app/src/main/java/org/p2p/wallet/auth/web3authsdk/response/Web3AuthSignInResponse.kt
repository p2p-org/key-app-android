package org.p2p.wallet.auth.web3authsdk.response

import com.google.gson.annotations.SerializedName

data class Web3AuthSignInResponse(
    @SerializedName("ethAddress")
    val ethereumPublicKey: String,
    // bip39 mnemonic
    @SerializedName("privateSOL")
    val mnemonicPhrase: String,
)
