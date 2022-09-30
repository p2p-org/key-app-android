package org.p2p.wallet.auth.web3authsdk

import com.google.gson.JsonObject
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignInResponse
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignUpResponse

interface Web3AuthApi {
    class Web3AuthSdkInternalError(override val message: String, override val cause: Throwable? = null) : Throwable()

    suspend fun triggerSilentSignUp(
        torusKey: String
    ): Web3AuthSignUpResponse

    suspend fun triggerSignInNoCustom(
        socialShare: String,
        deviceShare: Web3AuthSignUpResponse.ShareDetailsWithMeta,
    ): Web3AuthSignInResponse

    suspend fun triggerSignInNoTorus(
        deviceShare: Web3AuthSignUpResponse.ShareDetailsWithMeta,
        thirdShare: Web3AuthSignUpResponse.ShareDetailsWithMeta,
        encryptedMnemonic: JsonObject
    ): Web3AuthSignInResponse

    suspend fun triggerSignInNoDevice(
        socialShare: String,
        thirdShare: Web3AuthSignUpResponse.ShareDetailsWithMeta,
        encryptedMnemonic: JsonObject
    ): Web3AuthSignInResponse

    suspend fun obtainTorusKey(
        googleUserTokenId: String
    ): String
}
