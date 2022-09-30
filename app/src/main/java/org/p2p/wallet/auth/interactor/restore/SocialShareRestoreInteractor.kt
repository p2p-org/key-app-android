package org.p2p.wallet.auth.interactor.restore

import org.p2p.wallet.auth.repository.RestoreFlowDataLocalRepository
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage

class SocialShareRestoreInteractor(
    private val restoreFlowDataLocalRepository: RestoreFlowDataLocalRepository,
    private val userSignUpDetailsStorage: UserSignUpDetailsStorage,
) {
    fun restoreSocialShare(torusKey: String, socialShareUserId: String) {
        restoreFlowDataLocalRepository.also {
            it.torusKey = torusKey
            it.socialShareUserId = socialShareUserId
            it.deviceShare = userSignUpDetailsStorage.getLastSignUpUserDetails()
                ?.signUpDetails
                ?.deviceShare
        }
    }
}
