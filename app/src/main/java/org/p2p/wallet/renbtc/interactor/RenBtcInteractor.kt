package org.p2p.wallet.renbtc.interactor

import kotlinx.coroutines.flow.Flow
import org.p2p.solanaj.kits.renBridge.LockAndMint
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.renbtc.RenTransactionManager
import org.p2p.wallet.renbtc.model.RenTransaction
import org.p2p.wallet.renbtc.model.RenTransactionStatus
import org.p2p.wallet.renbtc.repository.RenBTCRepository

class RenBtcInteractor(
    private val repository: RenBTCRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val renTransactionManager: RenTransactionManager
) {

    fun getAllTransactions(): List<RenTransaction> = renTransactionManager.getAllTransactions()
        .mapNotNull { transaction ->
            val transactionStatuses = renTransactionManager.getTransactionStatuses(transaction.transactionId)
            transactionStatuses?.lastOrNull()?.isSuccessAndPastMinuteAgo() ?: return@mapNotNull null
            transaction
        }

    fun getSessionFlow(): Flow<LockAndMint.Session?> {
        val signer = tokenKeyProvider.publicKey
        return repository.findSessionFlow(signer)
    }

    fun getTransactionStatuses(transactionHash: String): List<RenTransactionStatus>? =
        renTransactionManager.getTransactionStatuses(transactionHash)

    suspend fun findActiveSession(): LockAndMint.Session? {
        val signer = tokenKeyProvider.publicKey
        return repository.findSession(signer)
    }

    suspend fun clearSession() {
        repository.clearSessionData()
    }

    suspend fun generateSession(): LockAndMint.Session =
        renTransactionManager.initializeSession(null)

    suspend fun startSession(session: LockAndMint.Session): LockAndMint.Session =
        renTransactionManager.initializeSession(session)

    suspend fun startPolling(session: LockAndMint.Session) {
        renTransactionManager.startPolling(session)
    }
}