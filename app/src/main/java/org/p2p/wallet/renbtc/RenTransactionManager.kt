package org.p2p.wallet.renbtc

import org.p2p.solanaj.kits.renBridge.LockAndMint
import org.p2p.solanaj.kits.renBridge.renVM.RenVMRepository
import org.p2p.solanaj.rpc.RpcSolanaInteractor
import org.p2p.wallet.auth.analytics.RenBtcAnalytics
import org.p2p.core.network.environment.NetworkEnvironment
import org.p2p.core.network.environment.NetworkEnvironmentManager
import org.p2p.wallet.renbtc.model.RenBTCPayment
import org.p2p.wallet.renbtc.model.RenTransaction
import org.p2p.wallet.renbtc.model.RenTransactionStatus
import org.p2p.wallet.renbtc.repository.RenRemoteRepository
import org.p2p.wallet.renbtc.service.RenStatusExecutor
import org.p2p.wallet.renbtc.service.RenTransactionExecutor
import org.p2p.wallet.utils.toPublicKey
import timber.log.Timber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val SESSION_POLLING_DELAY = 5000L

/**
 * [RenTransactionManager] is responsible for executing transactions via Bitcoin network
 * It polls the certain endpoint to check if there are new transactions received
 * It executes transactions by order, once the first is minted, the second transaction is started
 * */

class RenTransactionManager(
    private val renBTCRemoteRepository: RenRemoteRepository,
    private val environmentManager: NetworkEnvironmentManager,
    private val renVMRepository: RenVMRepository,
    private val solanaChain: RpcSolanaInteractor,
    private val renBtcAnalytics: RenBtcAnalytics
) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private const val REN_TAG = "renBTC"
    }

    private lateinit var lockAndMint: LockAndMint

    private val executors = HashSet<RenTransactionExecutor>()

    private var queuedTransactions = mutableListOf<RenTransaction>()

    suspend fun initializeSession(existingSession: LockAndMint.Session?, signer: String): LockAndMint.Session =
        withContext(scope.coroutineContext) {
            lockAndMint = if (existingSession == null || !existingSession.isValid) {
                val session = LockAndMint.Session(signer.toPublicKey())
                Timber.tag(REN_TAG).d("No existing session found, building new one")
                LockAndMint.buildSession(
                    renVMRepository = renVMRepository,
                    session = session,
                    solanaChain = solanaChain,
                    state = LockAndMint.State()
                )
            } else {
                Timber.tag(REN_TAG).d("Active session found, fetching information")
                LockAndMint.getSession(
                    renVMRepository = renVMRepository,
                    session = existingSession,
                    solanaChain = solanaChain,
                    state = LockAndMint.State()
                )
            }

            val gatewayAddress = lockAndMint.generateGatewayAddress(environmentManager.loadRpcEnvironment())
            Timber.tag(REN_TAG).d("Gateway address generated: $gatewayAddress")

            val fee = lockAndMint.estimateTransactionFee()
            Timber.tag(REN_TAG).d("Fee calculated: $fee")

            return@withContext lockAndMint.getSession()
        }

    suspend fun startPolling(session: LockAndMint.Session, secretKey: ByteArray) = withContext(scope.coroutineContext) {
        if (!::lockAndMint.isInitialized) throw IllegalStateException("LockAndMint object is not initialized")
        Timber.tag(REN_TAG).d("Starting blockstream polling")

        val environment = environmentManager.loadCurrentEnvironment()

        /* Caching value, since it's being called multiple times inside the loop */
        while (session.isValid) {
            pollPaymentData(environment, session, secretKey)
            delay(SESSION_POLLING_DELAY)
        }
    }

    fun getAllTransactions(): List<RenTransaction> = queuedTransactions

    fun getTransactionStatuses(transactionId: String): List<RenTransactionStatus>? {
        val transaction = queuedTransactions.firstOrNull { it.transactionId == transactionId }
        if (transaction == null) {
            Timber
                .tag(REN_TAG)
                .w("There are no transactions are being executed or current hash is wrong: $transactionId")
            return null
        }
        Timber.tag(REN_TAG).d("Transaction statuses = %s", queuedTransactions.toString())

        return transaction.statuses
    }

    fun stop() {
        scope.cancel()
        queuedTransactions.clear()
    }

    private fun pollPaymentData(environment: NetworkEnvironment, session: LockAndMint.Session, secretKey: ByteArray) {
        scope.launch {
            Timber.tag(REN_TAG).d("Checking payment data by gateway address")
            try {
                val data = renBTCRemoteRepository.getPaymentData(environment, session.gatewayAddress)
                Timber.tag(REN_TAG).d("Fetched data = $data")
                handlePaymentData(data, secretKey)
            } catch (e: Throwable) {
                Timber.e(e, "Error checking payment data")
            }
        }
    }

    private suspend fun handlePaymentData(data: List<RenBTCPayment>, secretKey: ByteArray) =
        withContext(scope.coroutineContext) {
            Timber.tag(REN_TAG).d("Payment data received: ${data.size}")

            /*
            * Filtering for duplicated transactions
            * */
            data.forEach { payment ->
                val alreadyExists = queuedTransactions.any { it.transactionId == payment.transactionHash }
                Timber.tag(REN_TAG).d("Transaction ${payment.transactionHash} is added or queued, skipping")
                if (alreadyExists) return@forEach

                RenTransaction(
                    transactionId = payment.transactionHash,
                    payment = payment
                ).also { queuedTransactions.add(it) }
            }

            /* Making sure we have transactions that should be executed */
            val awaitingTransactions = queuedTransactions.filter { it.isAwaiting() }
            if (awaitingTransactions.isEmpty()) return@withContext

            val executorsSize = executors.size
            Timber.tag(REN_TAG).d("Starting filter executors for finished one. Size: $executorsSize")
            /* Removing active executors to add new executor for new transaction */
            if (executorsSize != 0) {
                executors.removeAll {
                    val isFinished = it.isFinished()
                    Timber.tag(REN_TAG).d("Transaction ${it.getTransactionHash()} finished: $isFinished")
                    isFinished
                }
            }

            /* Making sure there are no any active executors, checking new executors size */
            if (executors.isNotEmpty()) {
                Timber.tag(REN_TAG).d("Filter finished, there are still active executors exist, waiting")
                return@withContext
            }

            Timber.tag(REN_TAG).d("No active executors, adding new transaction executor")
            /* executors list includes only active or new transactions */
            queuedTransactions.forEach { transaction ->
                executors.add(
                    RenStatusExecutor(
                        lockAndMint = lockAndMint,
                        transaction = transaction,
                        secretKey = secretKey,
                        renBtcAnalytics = renBtcAnalytics
                    )
                )
            }

            Timber.tag(REN_TAG).d("Starting execution, executors new count: ${executors.size}")
            /*
             * Each transaction is being executed in separate coroutine
             * */
            executors.forEach {
                launch { it.execute() }
            }
        }
}
