package org.p2p.wallet.renbtc.repository

import kotlinx.coroutines.flow.Flow
import org.p2p.solanaj.kits.renBridge.LockAndMint

interface RenLoaclRepository {
    suspend fun saveSession(session: LockAndMint.Session)
    suspend fun clearSessionData()
    fun findSessionFlow(destinationAddress: String): Flow<LockAndMint.Session?>
    suspend fun findSession(destinationAddress: String): LockAndMint.Session?
}
