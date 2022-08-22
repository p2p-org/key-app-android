package org.p2p.wallet.auth.web3authsdk

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

    fun triggerSilentSignUp(socialShare: String, handler: Web3AuthSignUpCallback)
    fun triggerSignInNoDevice(socialShare: String)
    fun triggerSignInNoCustom(socialShare: String, deviceShare: String)
}
