package org.p2p.wallet.auth.web3authsdk

import com.google.gson.JsonObject
import org.p2p.wallet.auth.model.Web3AuthSignInResponse
import org.p2p.wallet.auth.model.Web3AuthSignUpResponse

interface Web3AuthApi {
    class Web3AuthSdkInternalError(override val message: String, override val cause: Throwable? = null) : Throwable()

    interface Web3AuthClientHandler {
        fun handleApiError(error: Web3AuthErrorResponse)
        fun handleInternalError(internalError: Web3AuthSdkInternalError)
    }

    interface Web3AuthSignUpCallback : Web3AuthClientHandler {
        fun onSuccessSignUp(signUpResponse: Web3AuthSignUpResponse)
    }

    interface Web3AuthSignInCallback : Web3AuthClientHandler {
        fun onSuccessSignIn(signInResponse: Web3AuthSignInResponse)
    }

    fun triggerSilentSignUp(
        socialShare: String,
        handler: Web3AuthSignUpCallback
    )
    fun triggerSignInNoTorus(
        deviceShare: Web3AuthSignUpResponse.ShareDetailsWithMeta,
        thirdShare: Web3AuthSignUpResponse.ShareDetailsWithMeta,
        encryptedMnemonicPhrase: JsonObject,
        handler: Web3AuthSignInCallback
    )
    fun triggerSignInNoCustom(
        socialShare: String,
        deviceShare: Web3AuthSignUpResponse.ShareDetailsWithMeta,
        handler: Web3AuthSignInCallback
    )
    fun triggerSignInNoDevice(
        socialShare: String,
        thirdShare: Web3AuthSignUpResponse.ShareDetailsWithMeta,
        handler: Web3AuthSignInCallback
    )
}
