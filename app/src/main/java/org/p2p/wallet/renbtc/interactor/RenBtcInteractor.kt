package org.p2p.wallet.renbtc.interactor

import kotlinx.coroutines.flow.Flow
import org.p2p.solanaj.kits.renBridge.LockAndMint
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.renbtc.RenTransactionManager
import org.p2p.wallet.renbtc.model.RenTransaction
import org.p2p.wallet.renbtc.model.RenTransactionStatus
import org.p2p.wallet.renbtc.repository.RenBTCRepository
import timber.log.Timber

class RenBtcInteractor(
    private val repository: RenBTCRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val renTransactionManager: RenTransactionManager
) {

    fun getAllTransactions(): List<RenTransaction> = renTransactionManager.getAllTransactions().mapNotNull {
        val latestStatus = renTransactionManager.getLatestState(it.transactionId) ?: return@mapNotNull null
        if (latestStatus.isSuccessAndPastMinuteAgo()) return@mapNotNull null
        it.copy(status = latestStatus)
    }

    fun getSessionFlow(): Flow<LockAndMint.Session?> {
        val signer = tokenKeyProvider.publicKey
        return repository.findSessionFlow(signer)
    }

    fun getStateFlow(transactionHash: String): Flow<MutableList<RenTransactionStatus>>? =
        renTransactionManager.getStateFlow(transactionHash)

    suspend fun findActiveSession(): LockAndMint.Session? {
        val signer = tokenKeyProvider.publicKey
        return repository.findSession(signer)
    }

    suspend fun clearSession() {
        repository.clearSessionData()
    }

    suspend fun generateSession(): LockAndMint.Session =
        renTransactionManager.initializeSession()

    suspend fun startPolling(session: LockAndMint.Session) {
        renTransactionManager.startPolling(session)
    }
}