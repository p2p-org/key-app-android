package org.p2p.wallet.auth.interactor.restore

import org.p2p.wallet.auth.repository.RestoreFlowDataLocalRepository

class SocialShareRestoreInteractor(
    private val restoreFlowDataLocalRepository: RestoreFlowDataLocalRepository
) {
    fun restoreSocialShare(socialShare: String, socialShareUserId: String) {
        restoreFlowDataLocalRepository.also {
            it.socialShare = socialShare
            it.socialShareUserId = socialShareUserId
        }
    }
}
