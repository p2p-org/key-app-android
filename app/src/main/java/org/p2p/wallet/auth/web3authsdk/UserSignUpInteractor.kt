package org.p2p.wallet.auth.web3authsdk

import kotlinx.coroutines.suspendCancellableCoroutine
import org.p2p.wallet.auth.interactor.SignUpFlowDataCache
import org.p2p.wallet.auth.model.Web3AuthSignUpResponse
import kotlin.coroutines.resume

class UserSignUpInteractor(
    private val web3AuthApi: Web3AuthApiClient,
    private val userSignUpDetailsStorage: UserSignUpDetailsStorage,
    private val signUpFlowDataCache: SignUpFlowDataCache
) {

    sealed class SignUpResult {
        object UserAlreadyExists : SignUpResult()
        object SignUpSuccessful : SignUpResult()
        class SignUpFailed(val message: String?, val cause: Throwable? = null) : SignUpResult()
    }

    suspend fun trySignUpNewUser(idToken: String, idTokenOwnerId: String): SignUpResult {
        web3AuthApi.attach()
        return try {
            signUpFlowDataCache.signUpUserId = idTokenOwnerId

            val signUpResponse: Web3AuthSignUpResponse = generateDeviceAndThirdShare(idToken)
            signUpFlowDataCache.generateUserAccount(userMnemonicPhrase = signUpResponse.mnemonicPhrase.split(""))

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
            signUpFlowDataCache.signUpUserId = it.userId
            signUpFlowDataCache.generateUserAccount(
                userMnemonicPhrase = it.signUpDetails.mnemonicPhrase.split("")
            )
        } ?: return SignUpResult.UserAlreadyExists

        return SignUpResult.SignUpSuccessful
    }

    private suspend fun generateDeviceAndThirdShare(idToken: String): Web3AuthSignUpResponse {
        return suspendCancellableCoroutine { continuation ->
            web3AuthApi.triggerSilentSignUp(
                socialShare = idToken,
                object : Web3AuthApiClient.Web3AuthSignUpCallback {
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
