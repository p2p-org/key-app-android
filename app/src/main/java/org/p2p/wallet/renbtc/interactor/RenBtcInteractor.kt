package org.p2p.wallet.renbtc.interactor

import org.p2p.solanaj.kits.renBridge.LockAndMint
import org.p2p.core.network.environment.NetworkEnvironment
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.renbtc.RenTransactionManager
import org.p2p.wallet.renbtc.model.RenBtcSession
import org.p2p.wallet.renbtc.model.RenTransaction
import org.p2p.wallet.renbtc.model.RenTransactionStatus
import org.p2p.wallet.renbtc.repository.RenLoaclRepository
import org.p2p.wallet.renbtc.repository.RenRepository
import org.p2p.wallet.renbtc.repository.RenStateLocalRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class RenBtcInteractor(
    private val repository: RenRepository,
    private val databaseRepository: RenLoaclRepository,
    private val localRepository: RenStateLocalRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val renTransactionManager: RenTransactionManager
) {

    fun getAllTransactions(): List<RenTransaction> = renTransactionManager.getAllTransactions()
        .mapNotNull { transaction ->
            val transactionStatuses = renTransactionManager.getTransactionStatuses(transaction.transactionId)
            transactionStatuses?.lastOrNull()?.isSuccessAndPastMinuteAgo() ?: return@mapNotNull null
            transaction
        }

    @FlowPreview
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

    suspend fun isUserHasActiveSession(): Boolean = findActiveSession().let { it != null && it.isValid }

    suspend fun clearSession() {
        databaseRepository.clearSessionData()
    }

    suspend fun generateSession(): LockAndMint.Session = coroutineScope {
        databaseRepository.clearSessionData()
        val session = renTransactionManager.initializeSession(null, tokenKeyProvider.publicKey)
        setSessionSate(RenBtcSession.Active(session))
        session
    }

    suspend fun startSession(lockAndMintSession: LockAndMint.Session): LockAndMint.Session = coroutineScope {
        databaseRepository.clearSessionData()
        val renSession = renTransactionManager.initializeSession(lockAndMintSession, tokenKeyProvider.publicKey)
        setSessionSate(RenBtcSession.Active(renSession))
        renSession
    }

    suspend fun startPolling(session: LockAndMint.Session) {
        renTransactionManager.startPolling(session, tokenKeyProvider.keyPair)
    }

    suspend fun getPaymentData(environment: NetworkEnvironment, gatewayAddress: String) =
        repository.getPaymentData(environment, gatewayAddress)

    suspend fun setSessionSate(session: RenBtcSession) {
        when (session) {
            is RenBtcSession.Active -> databaseRepository.saveSession(session.session)
            else -> localRepository.saveSession(session)
        }
    }
}
