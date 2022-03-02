package org.p2p.wallet.renbtc.repository

import kotlinx.coroutines.flow.Flow
import org.p2p.wallet.renbtc.model.RenBtcSession

interface RenBtcLocalRepository {
    fun saveSession(session: RenBtcSession)
    fun getSessionFlow(): Flow<RenBtcSession>
}