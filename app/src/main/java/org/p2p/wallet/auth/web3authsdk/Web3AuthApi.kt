package org.p2p.wallet.auth.web3authsdk

import org.p2p.wallet.auth.model.Web3AuthSignUpResponse

interface Web3AuthApi {
    class Web3AuthSdkInternalError(override val message: String, override val cause: Throwable? = null) : Throwable()

    interface Web3AuthClientHandler {
        fun handleError(error: Web3AuthErrorResponse)
    }

    interface Web3AuthSignUpCallback : Web3AuthClientHandler {
        fun onSuccessSignUp(signUpResponse: Web3AuthSignUpResponse)
    }

    fun attach()
    fun detach()

    fun triggerSilentSignUp(socialShare: String, handler: Web3AuthSignUpCallback)
    fun triggerSignInNoDevice(socialShare: String)
    fun triggerSignInNoCustom(socialShare: String, deviceShare: String)
}
