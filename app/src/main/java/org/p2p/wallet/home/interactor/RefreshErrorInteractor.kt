package org.p2p.wallet.home.interactor

import org.p2p.wallet.home.repository.RefreshErrorRepository

class RefreshErrorInteractor(
    private val refreshErrorRepository: RefreshErrorRepository
) {
    fun getRefreshClickFlow() = refreshErrorRepository.getFlow()

    fun notifyRefreshClick() = refreshErrorRepository.notifyClick()
}
