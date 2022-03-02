package org.p2p.wallet.renbtc.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.wallet.renbtc.model.RenBtcSession

class RenBtcDaoRepository : RenBtcLocalRepository {

    private val sessionFlow = MutableStateFlow<RenBtcSession>(RenBtcSession.Loading)

    override fun saveSession(session: RenBtcSession) {
        sessionFlow.value = session
    }

    override fun getSessionFlow(): Flow<RenBtcSession> = sessionFlow
}