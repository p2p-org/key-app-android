package org.p2p.wallet.home.interactor

import kotlinx.coroutines.flow.Flow
import org.p2p.wallet.home.repository.RefreshErrorRepository

class RefreshErrorInteractor(
    private val refreshErrorRepository: RefreshErrorRepository
) {
    fun getRefreshEventFlow(): Flow<Unit> = refreshErrorRepository.getRefreshEventFlow()

    fun notifyEventRefreshed(): Unit = refreshErrorRepository.notifyEventRefreshed()
}
