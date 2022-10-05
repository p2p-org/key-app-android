package org.p2p.wallet.auth.interactor.restore

import org.p2p.wallet.auth.interactor.OnboardingInteractor
import org.p2p.wallet.auth.model.OnboardingFlow
import org.p2p.wallet.auth.repository.RestoreFlowDataLocalRepository
import org.p2p.wallet.auth.repository.SignUpFlowDataLocalRepository
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage
import org.p2p.wallet.auth.web3authsdk.Web3AuthApi
import timber.log.Timber

class TorusKeyInteractor(
    private val restoreFlowDataLocalRepository: RestoreFlowDataLocalRepository,
    private val userSignUpDetailsStorage: UserSignUpDetailsStorage,
    private val createFlowDataLocalRepository: SignUpFlowDataLocalRepository,
    private val onboardingInteractor: OnboardingInteractor,
    private val web3AuthApi: Web3AuthApi
) {
    suspend fun getTorusKey(googleSocialToken: String, socialShareUserId: String) {
        try {
            val torusKey = web3AuthApi.obtainTorusKey(googleSocialToken)
            Timber.tag("_______getTorusKey").d(socialShareUserId)

            when (onboardingInteractor.currentFlow) {
                is OnboardingFlow.CreateWallet -> {
                    createFlowDataLocalRepository.torusKey = torusKey
                    createFlowDataLocalRepository.signUpUserId = socialShareUserId
                }
                is OnboardingFlow.RestoreWallet -> {
                    restoreFlowDataLocalRepository.also {
                        it.torusKey = torusKey
                        it.socialShareUserId = socialShareUserId
                        it.deviceShare = userSignUpDetailsStorage.getLastSignUpUserDetails()
                            ?.signUpDetails
                            ?.deviceShare
                    }
                }
            }
            Timber.i("Torus key obtained")
        } catch (e: Throwable) {
            Timber.i("Error on obtain a torus key $e")
        }
    }
}
