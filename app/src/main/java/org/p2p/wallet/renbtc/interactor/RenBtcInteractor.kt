package org.p2p.wallet.renbtc.interactor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.p2p.solanaj.kits.renBridge.LockAndMint
import org.p2p.solanaj.rpc.Environment
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.renbtc.RenTransactionManager
import org.p2p.wallet.renbtc.model.RenBtcSession
import org.p2p.wallet.renbtc.model.RenTransaction
import org.p2p.wallet.renbtc.model.RenTransactionStatus
import org.p2p.wallet.renbtc.repository.RenBTCRepository
import org.p2p.wallet.renbtc.repository.RenBtcDatabaseRepository
import org.p2p.wallet.renbtc.repository.RenBtcLocalRepository

class RenBtcInteractor(
    private val repository: RenBTCRepository,
    private val databaseRepository: RenBtcDatabaseRepository,
    private val localRepository: RenBtcLocalRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val renTransactionManager: RenTransactionManager
) {

    fun getAllTransactions(): List<RenTransaction> = renTransactionManager.getAllTransactions()
        .mapNotNull { transaction ->
            val transactionStatuses = renTransactionManager.getTransactionStatuses(transaction.transactionId)
            transactionStatuses?.lastOrNull()?.isSuccessAndPastMinuteAgo() ?: return@mapNotNull null
            transaction
        }

    fun getSessionFlow(): Flow<RenBtcSession> {
        val signer = tokenKeyProvider.publicKey
        val daoFlow = databaseRepository.findSessionFlow(signer).filterNotNull().map { RenBtcSession.Active(it) }
        val localFlow = localRepository.getSessionFlow()
        return flowOf(daoFlow, localFlow).flattenMerge()
    }

    fun getTransactionStatuses(transactionHash: String): List<RenTransactionStatus>? =
        renTransactionManager.getTransactionStatuses(transactionHash)

    suspend fun findActiveSession(): LockAndMint.Session? {
        val signer = tokenKeyProvider.publicKey
        return databaseRepository.findSession(signer)
    }

    suspend fun clearSession() {
        databaseRepository.clearSessionData()
    }

    suspend fun generateSession(): LockAndMint.Session {
        databaseRepository.clearSessionData()
        val session = renTransactionManager.initializeSession(null)
        setSessionSate(RenBtcSession.Active(session))
        return session
    }

    suspend fun startSession(session: LockAndMint.Session): LockAndMint.Session {
        databaseRepository.clearSessionData()
        val session = renTransactionManager.initializeSession(session)
        setSessionSate(RenBtcSession.Active(session))
        return session
    }

    suspend fun startPolling(session: LockAndMint.Session) {
        renTransactionManager.startPolling(session)
    }

    suspend fun getPaymentData(environment: Environment, gatewayAddress: String) =
        repository.getPaymentData(environment, gatewayAddress)

    suspend fun setSessionSate(session: RenBtcSession) {
        when (session) {
            is RenBtcSession.Active -> databaseRepository.saveSession(session.session)
            else -> localRepository.saveSession(session)
        }
    }
}