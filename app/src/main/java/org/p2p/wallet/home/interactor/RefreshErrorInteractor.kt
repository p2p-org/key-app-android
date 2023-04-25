package org.p2p.wallet.home.interactor

import kotlinx.coroutines.flow.Flow
import org.p2p.wallet.home.repository.RefreshErrorRepository

class RefreshErrorInteractor(
    private val refreshErrorRepository: RefreshErrorRepository
) {
    fun getRefreshClickFlow(): Flow<Unit> = refreshErrorRepository.getRefreshClickFlow()

    fun notifyRefreshClicked(): Unit = refreshErrorRepository.notifyRefreshClicked()
}
