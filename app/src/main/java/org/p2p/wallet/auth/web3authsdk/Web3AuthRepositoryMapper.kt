package org.p2p.wallet.auth.web3authsdk

import com.google.gson.Gson
import org.p2p.wallet.auth.model.Web3AuthErrorResponse
import org.p2p.wallet.auth.model.Web3AuthSignUpResponse
import org.p2p.wallet.utils.fromJsonReified
import timber.log.Timber

class Web3AuthRepositoryMapper(private val gson: Gson) {
    fun fromNetworkSignUp(responseJson: String): Web3AuthSignUpResponse? {
        return gson.fromJsonReified<Web3AuthSignUpResponse>(responseJson)
    }

    fun fromNetworkError(errorResponseJson: String): Web3AuthErrorResponse {
        return gson.fromJsonReified<Web3AuthErrorResponse>(errorResponseJson)
            ?.let { createFilledResponseWithErrorType(it) }
            ?: error("Response mapping failed from: $errorResponseJson")
    }

    private fun createFilledResponseWithErrorType(response: Web3AuthErrorResponse): Web3AuthErrorResponse {
        val errorType = Web3AuthErrorResponse.ErrorType.findByCode(response.errorCode)
        if (errorType == Web3AuthErrorResponse.ErrorType.UNDEFINED) {
            Timber.tag("Web3AuthErrorMapper").e("No code found for response: ${response.errorCode}")
        }
        return response.copy(errorType = errorType)
    }
}
