package org.p2p.wallet.auth.web3authsdk

import kotlinx.coroutines.suspendCancellableCoroutine
import org.p2p.wallet.auth.model.Web3AuthSignUpResponse
import org.p2p.wallet.auth.repository.SignUpFlowDataLocalRepository
import org.p2p.wallet.auth.web3authsdk.Web3AuthErrorResponse.ErrorType
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class UserSignUpInteractor(
    private val web3AuthApi: Web3AuthApi,
    private val userSignUpDetailsStorage: UserSignUpDetailsStorage,
    private val signUpFlowDataRepository: SignUpFlowDataLocalRepository
) {

    sealed class SignUpResult {
        object UserAlreadyExists : SignUpResult()
        object SignUpSuccessful : SignUpResult()
        class SignUpFailed(val message: String?, val cause: Throwable? = null) : SignUpResult()
    }

    suspend fun trySignUpNewUser(idToken: String, idTokenOwnerId: String): SignUpResult {
        return try {
            signUpFlowDataRepository.signUpUserId = idTokenOwnerId

            val signUpResponse: Web3AuthSignUpResponse = generateDeviceAndThirdShare(idToken)
            signUpFlowDataRepository.generateUserAccount(userMnemonicPhrase = signUpResponse.mnemonicPhrase.split(""))

            userSignUpDetailsStorage.save(signUpResponse, idTokenOwnerId)
            SignUpResult.SignUpSuccessful
        } catch (web3AuthError: Web3AuthErrorResponse) {
            if (web3AuthError.errorType == ErrorType.CANNOT_RECONSTRUCT) {
                SignUpResult.UserAlreadyExists
            } else {
                SignUpResult.SignUpFailed(web3AuthError.errorMessage)
            }
        } catch (error: Exception) {
            SignUpResult.SignUpFailed(error.message, error.cause)
        }
    }

    fun continueSignUpUser(): SignUpResult {
        userSignUpDetailsStorage.getLastSignUpUserDetails()?.let {
            signUpFlowDataRepository.signUpUserId = it.userId
            signUpFlowDataRepository.generateUserAccount(
                userMnemonicPhrase = it.signUpDetails.mnemonicPhrase.split("")
            )
        } ?: return SignUpResult.UserAlreadyExists

        return SignUpResult.SignUpSuccessful
    }

    private suspend fun generateDeviceAndThirdShare(idToken: String): Web3AuthSignUpResponse {
        return suspendCancellableCoroutine { continuation ->
            web3AuthApi.triggerSilentSignUp(
                socialShare = idToken,
                handler = object : Web3AuthApi.Web3AuthSignUpCallback {
                    override fun onSuccessSignUp(signUpResponse: Web3AuthSignUpResponse) {
                        continuation.resume(signUpResponse)
                    }

                    override fun handleApiError(error: Web3AuthErrorResponse) {
                        continuation.resumeWithException(error)
                    }

                    override fun handleInternalError(internalError: Web3AuthApi.Web3AuthSdkInternalError) {
                        continuation.resumeWithException(internalError)
                    }
                }
            )
        }
    }
}
