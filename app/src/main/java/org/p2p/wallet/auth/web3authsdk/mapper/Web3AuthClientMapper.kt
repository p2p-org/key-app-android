package org.p2p.wallet.auth.web3authsdk.mapper

import com.google.gson.Gson
import org.p2p.wallet.auth.web3authsdk.Web3AuthApi.Web3AuthSdkInternalError
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthErrorResponse
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignInResponse
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignUpResponse
import timber.log.Timber
import org.p2p.core.utils.fromJsonReified

private const val INTERNAL_ERROR_MESSAGE = "Web3Auth mapping failed"

class Web3AuthClientMapper(private val gson: Gson) {
    fun fromNetworkSignUp(responseJson: String): Result<Web3AuthSignUpResponse> = try {
        Result.success(gson.fromJsonReified<Web3AuthSignUpResponse>(responseJson)!!)
    } catch (mappingError: Throwable) {
        Timber.i(mappingError)
        Timber.i(responseJson)
        Result.failure(Web3AuthSdkInternalError(INTERNAL_ERROR_MESSAGE, mappingError))
    }

    fun obtainTorusKey(responseString: String): Result<String> = try {
        Result.success(responseString)
    } catch (error: Throwable) {
        Timber.i(error)
        Result.failure(Web3AuthSdkInternalError(INTERNAL_ERROR_MESSAGE, error))
    }

    fun fromNetworkSignIn(responseJson: String): Result<Web3AuthSignInResponse> = try {
        Result.success(gson.fromJsonReified<Web3AuthSignInResponse>(responseJson)!!)
    } catch (mappingError: Throwable) {
        Timber.i(mappingError)
        Timber.i(responseJson)
        Result.failure(Web3AuthSdkInternalError(INTERNAL_ERROR_MESSAGE, mappingError))
    }

    fun fromNetworkError(errorResponseJson: String): Web3AuthErrorResponse = try {
        val web3AuthError = gson.fromJsonReified<Web3AuthErrorResponse>(errorResponseJson)!!
        createFilledResponseWithErrorType(web3AuthError)
    } catch (mappingError: Throwable) {
        Timber.i(mappingError)
        Timber.i(errorResponseJson)
        throw Web3AuthSdkInternalError(INTERNAL_ERROR_MESSAGE, mappingError)
    }

    private fun createFilledResponseWithErrorType(response: Web3AuthErrorResponse): Web3AuthErrorResponse {
        val errorType = Web3AuthErrorResponse.ErrorType.findByCode(response.errorCode)
        if (errorType == Web3AuthErrorResponse.ErrorType.UNDEFINED) {
            Timber.tag("Web3AuthErrorMapper").e("No code found for response: ${response.errorCode}")
        }
        return response.copy(errorType = errorType)
    }

    fun fromNetworkRefreshDeviceShare(responseJson: String): Result<Web3AuthSignUpResponse.ShareDetailsWithMeta> = try {
        Result.success(gson.fromJsonReified<Web3AuthSignUpResponse.ShareDetailsWithMeta>(responseJson)!!)
    } catch (mappingError: Throwable) {
        Timber.i(mappingError)
        Timber.i(responseJson)
        Result.failure(Web3AuthSdkInternalError(INTERNAL_ERROR_MESSAGE, mappingError))
    }
}
