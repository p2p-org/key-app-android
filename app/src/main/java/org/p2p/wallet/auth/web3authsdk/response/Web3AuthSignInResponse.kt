package org.p2p.wallet.auth.web3authsdk.response

import com.google.gson.annotations.SerializedName

private const val SEED_PHRASE_WORDS_SEPARATOR = " "

data class Web3AuthSignInResponse(
    @SerializedName("ethAddress")
    val ethereumPublicKey: String,
    // bip39 mnemonic
    @SerializedName("privateSOL")
    val mnemonicPhrase: String,
) {
    val mnemonicPhraseWords: List<String>
        get() = mnemonicPhrase.split(SEED_PHRASE_WORDS_SEPARATOR)
}
