package org.p2p.wallet.auth.web3authsdk.mapper

import com.google.gson.Gson
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthErrorResponse
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignInResponse
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignUpResponse
import org.p2p.wallet.utils.fromJsonReified
import timber.log.Timber

class Web3AuthClientMapper(private val gson: Gson) {
    fun fromNetworkSignUp(responseJson: String): Web3AuthSignUpResponse? {
        return kotlin.runCatching { gson.fromJsonReified<Web3AuthSignUpResponse>(responseJson) }
            .onFailure { Timber.i(it) }
            .getOrNull()
    }

    fun fromNetworkSignIn(responseJson: String): Web3AuthSignInResponse? {
        return kotlin.runCatching { gson.fromJsonReified<Web3AuthSignInResponse>(responseJson) }
            .onFailure { Timber.i(it) }
            .getOrNull()
    }

    fun fromNetworkError(errorResponseJson: String): Web3AuthErrorResponse? {
        return kotlin.runCatching { gson.fromJsonReified<Web3AuthErrorResponse>(errorResponseJson) }
            .onFailure { Timber.i(it) }
            .getOrNull()
            ?.let { createFilledResponseWithErrorType(it) }
    }

    private fun createFilledResponseWithErrorType(response: Web3AuthErrorResponse): Web3AuthErrorResponse {
        val errorType = Web3AuthErrorResponse.ErrorType.findByCode(response.errorCode)
        if (errorType == Web3AuthErrorResponse.ErrorType.UNDEFINED) {
            Timber.tag("Web3AuthErrorMapper").e("No code found for response: ${response.errorCode}")
        }
        return response.copy(errorType = errorType)
    }
}
