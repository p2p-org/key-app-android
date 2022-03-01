package org.p2p.wallet.renbtc.repository

import org.p2p.wallet.renbtc.model.RenBTCPayment
import kotlinx.coroutines.flow.Flow
import org.p2p.solanaj.kits.renBridge.LockAndMint
import org.p2p.solanaj.rpc.Environment

interface RenBTCRepository {
    suspend fun getPaymentData(environment: Environment, gateway: String): List<RenBTCPayment>
    suspend fun saveSession(session: LockAndMint.Session)
    suspend fun onErrorHandled(message: String)
    fun findSessionFlow(destinationAddress: String): Flow<LockAndMint.Session?>
    suspend fun findSession(destinationAddress: String): LockAndMint.Session?
    suspend fun clearSessionData()
}