package org.p2p.wallet.auth.web3authsdk

import kotlinx.coroutines.suspendCancellableCoroutine
import org.p2p.wallet.auth.model.Web3AuthSignUpResponse
import org.p2p.wallet.auth.repository.SignUpFlowDataLocalRepository
import kotlin.coroutines.resume

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
        web3AuthApi.attach()
        return try {
            signUpFlowDataRepository.signUpUserId = idTokenOwnerId

            val signUpResponse: Web3AuthSignUpResponse = generateDeviceAndThirdShare(idToken)
            signUpFlowDataRepository.generateUserAccount(userMnemonicPhrase = signUpResponse.mnemonicPhrase.split(""))

            userSignUpDetailsStorage.save(signUpResponse, idTokenOwnerId)
            SignUpResult.SignUpSuccessful
        } catch (web3AuthError: Web3AuthErrorResponse) {
            // TODO PWN-4268 do error handling right
            if (web3AuthError.errorCode == 9999) {
                SignUpResult.UserAlreadyExists
            } else {
                SignUpResult.SignUpFailed(web3AuthError.errorMessage)
            }
        } catch (error: Exception) {
            SignUpResult.SignUpFailed(error.message, error.cause)
        } finally {
            web3AuthApi.detach()
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
                object : Web3AuthApi.Web3AuthSignUpCallback {
                    override fun onSuccessSignUp(signUpResponse: Web3AuthSignUpResponse) {
                        continuation.resume(signUpResponse)
                    }

                    override fun handleError(error: Web3AuthErrorResponse) {
                        throw error
                    }
                }
            )
        }
    }
}
