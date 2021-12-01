package org.p2p.wallet.renbtc

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.p2p.solanaj.kits.renBridge.LockAndMint
import org.p2p.solanaj.kits.renBridge.NetworkConfig
import org.p2p.solanaj.rpc.Environment
import org.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.main.model.RenBTCPayment
import org.p2p.wallet.renbtc.model.RenTransaction
import org.p2p.wallet.renbtc.model.RenTransactionStatus
import org.p2p.wallet.renbtc.repository.RenBTCRepository
import org.p2p.wallet.renbtc.service.RenStatusExecutor
import org.p2p.wallet.renbtc.service.RenTransactionExecutor
import org.p2p.wallet.utils.toPublicKey
import timber.log.Timber

private const val SESSION_POLLING_DELAY = 5000L

class RenTransactionManager(
    private val repository: RenBTCRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val environmentManager: EnvironmentManager
) {

    companion object {
        const val REN_TAG = "renBTC"
    }

    private lateinit var lockAndMint: LockAndMint

    private val executors = HashSet<RenTransactionExecutor>()

    private var queuedTransactions = mutableListOf<RenTransaction>()

    suspend fun initializeSession(): LockAndMint.Session = withContext(Dispatchers.IO) {
        val signer = tokenKeyProvider.publicKey

        val existingSession = repository.findSession(signer)
        val networkConfig = getNetworkConfig()
        lockAndMint = if (existingSession == null || !existingSession.isValid) {
            repository.clearSessionData()
            Timber.tag(REN_TAG).d("No existing session found, building new one")
            LockAndMint.buildSession(networkConfig, signer.toPublicKey())
        } else {
            Timber.tag(REN_TAG).d("Active session found, fetching information")
            LockAndMint.getSession(networkConfig, existingSession)
        }

        val gatewayAddress = lockAndMint.generateGatewayAddress()
        Timber.tag(REN_TAG).d("Gateway address generated: $gatewayAddress")

        val fee = lockAndMint.estimateTransactionFee()
        Timber.tag(REN_TAG).d("Fee calculated: $fee")

        val session = lockAndMint.session

        repository.saveSession(session)
        return@withContext session
    }

    suspend fun startPolling(session: LockAndMint.Session) {
        if (!this::lockAndMint.isInitialized) throw IllegalStateException("LockAndMint object is not initialized")
        Timber.tag(REN_TAG).d("Starting blockstream polling")

        val environment = environmentManager.loadEnvironment()
        val secretKey = tokenKeyProvider.secretKey
        while (session.isValid) {
            val data = repository.getPaymentData(environment, session.gatewayAddress)
            handlePaymentData(data, secretKey)
            delay(SESSION_POLLING_DELAY)
        }
    }

    fun getAllTransactions(): List<RenTransaction> = queuedTransactions

    fun getStateFlow(transactionId: String): Flow<MutableList<RenTransactionStatus>>? {
        val executor = executors.firstOrNull { it.getTransactionHash() == transactionId }
        if (executor == null) {
            Timber
                .tag(REN_TAG)
                .w("There are no transactions are being executed or current hash is wrong: $transactionId")
            return null
        }

        return executor.getStateFlow()
    }

    fun getLatestState(transactionId: String): RenTransactionStatus? {
        val executor = executors.firstOrNull { it.getTransactionHash() == transactionId }
        if (executor == null) {
            Timber
                .tag(REN_TAG)
                .w("There are no transactions are being executed or current hash is wrong: $transactionId")
            return null
        }

        return executor.getStateFlow().value.lastOrNull()
    }

    private suspend fun handlePaymentData(
        data: List<RenBTCPayment>,
        secretKey: ByteArray
    ) = withContext(Dispatchers.IO) {
        Timber.tag(REN_TAG).d("Payment data received: ${data.size}")

        val filtered = data.mapNotNull { payment ->
            val alreadyExists = queuedTransactions.any { it.transactionId == payment.transactionHash }
            if (alreadyExists) return@mapNotNull null

            RenTransaction(
                transactionId = payment.transactionHash,
                payment = payment,
                status = RenTransactionStatus.WaitingDepositConfirm(payment.transactionHash)
            )
        }

        if (filtered.isEmpty()) return@withContext

        queuedTransactions.addAll(filtered)

        filtered.forEach { transaction ->
            val isNewPayment = executors.none { it.getTransactionHash() != transaction.transactionId }
            if (isNewPayment) {
                executors.add(RenStatusExecutor(lockAndMint, transaction.payment, secretKey))
            }
        }

        Timber.tag(REN_TAG).d("Starting execution, executors count: ${executors.size}")

        /*
         * Each transaction is being executed in separate coroutine
         * */
        executors.forEach {
            launch { it.execute() }
        }
    }

    private fun getNetworkConfig(): NetworkConfig =
        when (environmentManager.loadEnvironment()) {
            Environment.DEVNET -> NetworkConfig.DEVNET()
            Environment.RPC_POOL,
            Environment.MAINNET,
            Environment.SOLANA -> NetworkConfig.MAINNET()
        }
}