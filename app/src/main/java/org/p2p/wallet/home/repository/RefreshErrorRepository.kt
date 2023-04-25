package org.p2p.wallet.home.repository

import kotlinx.coroutines.flow.Flow

interface RefreshErrorRepository {
    fun getRefreshClickFlow(): Flow<Unit>
    fun notifyRefreshClicked()
}
