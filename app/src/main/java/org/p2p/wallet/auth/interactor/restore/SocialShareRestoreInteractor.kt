package org.p2p.wallet.auth.interactor.restore

import org.p2p.wallet.auth.repository.RestoreFlowDataLocalRepository
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage

class SocialShareRestoreInteractor(
    private val restoreFlowDataLocalRepository: RestoreFlowDataLocalRepository,
    private val userSignUpDetailsStorage: UserSignUpDetailsStorage,
) {
    fun restoreSocialShare(socialShare: String, socialShareUserId: String) {
        restoreFlowDataLocalRepository.also {
            it.socialShare = socialShare
            it.socialShareUserId = socialShareUserId
            it.deviceShare = userSignUpDetailsStorage.getUserSignUpDetailsById(socialShareUserId)
                ?.signUpDetails?.deviceShare
        }
    }
}
