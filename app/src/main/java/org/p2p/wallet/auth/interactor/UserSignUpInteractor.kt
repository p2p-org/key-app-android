package org.p2p.wallet.auth.interactor

import org.p2p.wallet.auth.repository.SignUpFlowDataLocalRepository
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage
import org.p2p.wallet.auth.web3authsdk.Web3AuthApi
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthErrorResponse
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthErrorResponse.ErrorType
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignUpResponse
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class UserSignUpInteractor(
    private val web3AuthApi: Web3AuthApi,
    private val userSignUpDetailsStorage: UserSignUpDetailsStorage,
    private val signUpFlowDataRepository: SignUpFlowDataLocalRepository
) {

    sealed class SignUpResult {
        object UserAlreadyExists : SignUpResult()
        object SignUpSuccessful : SignUpResult()
        class SignUpFailed(val cause: Throwable) : SignUpResult()
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
                SignUpResult.SignUpFailed(web3AuthError)
            }
        } catch (error: Exception) {
            SignUpResult.SignUpFailed(error)
        }
    }

    fun continueSignUpUser(): SignUpResult {
        return try {
            val lastUserDetails = userSignUpDetailsStorage.getLastSignUpUserDetails()
                ?: throw NullPointerException("Last sign up user details (aka device share) not found")

            signUpFlowDataRepository.signUpUserId = lastUserDetails.userId
            signUpFlowDataRepository.generateUserAccount(
                userMnemonicPhrase = lastUserDetails.signUpDetails.mnemonicPhrase.split("")
            )
            SignUpResult.SignUpSuccessful
        } catch (error: Exception) {
            SignUpResult.SignUpFailed(error)
        }
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
