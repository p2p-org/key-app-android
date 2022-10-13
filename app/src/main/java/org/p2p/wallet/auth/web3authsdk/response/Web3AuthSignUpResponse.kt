package org.p2p.wallet.auth.web3authsdk.response

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

private const val SEED_PHRASE_WORDS_SEPARATOR = " "

data class Web3AuthSignUpResponse(
    // Hex string
    @SerializedName("ethAddress") val ethereumPublicKey: String,
    // bip39 mnemonic
    @SerializedName("privateSOL") val mnemonicPhrase: String,
    // don't care about the type, we need raw json
    @SerializedName("metadata") val encryptedMnemonicPhrase: JsonObject,
    @SerializedName("deviceShare") val deviceShare: ShareDetailsWithMeta?,
    @SerializedName("customShare") val customThirdShare: ShareDetailsWithMeta?,
) {

    val mnemonicPhraseWords: List<String>
        get() = mnemonicPhrase.split(SEED_PHRASE_WORDS_SEPARATOR)

    data class ShareDetailsWithMeta(
        @SerializedName("share") val innerShareDetails: ShareInnerDetails,
        @SerializedName("p2p") val verifier: Verifier,
    ) {
        data class ShareInnerDetails(
            @SerializedName("share") val shareValue: ShareValue,
            @SerializedName("polynomialID") val polynomialID: String,
        ) {
            data class ShareValue(
                @SerializedName("share") val value: String,
                @SerializedName("shareIndex") val shareIndex: String,
            )
        }

        data class Verifier(
            @SerializedName("verifier") val verifier: String,
            @SerializedName("storage") val storage: String,
            @SerializedName("type") val type: String,
        )
    }
}
