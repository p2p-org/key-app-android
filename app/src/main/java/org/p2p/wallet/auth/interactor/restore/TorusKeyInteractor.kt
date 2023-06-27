package org.p2p.wallet.auth.interactor.restore

import timber.log.Timber
import org.p2p.wallet.auth.interactor.OnboardingInteractor
import org.p2p.wallet.auth.model.OnboardingFlow
import org.p2p.wallet.auth.repository.RestoreFlowDataLocalRepository
import org.p2p.wallet.auth.repository.SignUpFlowDataLocalRepository
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage
import org.p2p.wallet.auth.web3authsdk.Web3AuthApi

private const val TAG = "TorusKeyInteractor"

class TorusKeyInteractor(
    private val restoreFlowDataLocalRepository: RestoreFlowDataLocalRepository,
    private val userSignUpDetailsStorage: UserSignUpDetailsStorage,
    private val createFlowDataLocalRepository: SignUpFlowDataLocalRepository,
    private val onboardingInteractor: OnboardingInteractor,
    private val web3AuthApi: Web3AuthApi
) {
    private class GetTorusKeyFailure(
        override val message: String,
        override val cause: Throwable?
    ) : Throwable()

    suspend fun getTorusKey(googleIdJwtToken: String, socialShareUserId: String) {
        val currentFlow = onboardingInteractor.currentFlow
        try {
            Timber.tag(TAG).i("getTorusKey with flow: ${currentFlow.javaClass.simpleName}")
            val fetchedTorusKey = web3AuthApi.obtainTorusKey(googleIdJwtToken)

            when (onboardingInteractor.currentFlow) {
                is OnboardingFlow.CreateWallet -> fillCreateFlowData(fetchedTorusKey, socialShareUserId)
                is OnboardingFlow.RestoreWallet -> fillRestoreFlowData(fetchedTorusKey, socialShareUserId)
            }
            Timber.tag(TAG).i("Torus key obtained!")
        } catch (e: Throwable) {
            Timber.tag(TAG).e(
                GetTorusKeyFailure(
                    message = "Failed to get torus key for ${currentFlow.javaClass.simpleName}",
                    cause = e
                )
            )
        }
    }

    private fun fillCreateFlowData(fetchedTorusKey: String, socialShareUserId: String) {
        createFlowDataLocalRepository.torusKey = fetchedTorusKey
        createFlowDataLocalRepository.signUpUserId = socialShareUserId
    }

    private fun fillRestoreFlowData(fetchedTorusKey: String, socialShareUserId: String) {
        restoreFlowDataLocalRepository.also { repository ->
            repository.torusKey = fetchedTorusKey
            repository.socialShareUserId = socialShareUserId
            repository.deviceShare = userSignUpDetailsStorage.getLastSignUpUserDetails()
                ?.signUpDetails
                ?.deviceShare
        }
    }
}
