package com.p2p.wallet.main.interactor

import com.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import com.p2p.wallet.main.model.RenBTCPayment
import com.p2p.wallet.main.repository.RenBTCRepository
import com.p2p.wallet.utils.toPublicKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.kits.renBridge.LockAndMint
import org.p2p.solanaj.kits.renBridge.NetworkConfig
import timber.log.Timber

private const val TAG = "renBTC"
private const val SESSION_POLLING_DELAY = 5000L

class RenBTCInteractor(
    private val repository: RenBTCRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val environmentManager: EnvironmentManager
) {

    private lateinit var lockAndMint: LockAndMint

    private val state = MutableStateFlow<List<RenBTCPayment>>(emptyList())

    suspend fun getSession(): LockAndMint.Session? = withContext(Dispatchers.IO) {
        val signer = tokenKeyProvider.publicKey
        val existingSession = repository.findSession(signer) ?: return@withContext null

        val networkConfig = NetworkConfig.DEVNET()
        lockAndMint = LockAndMint.getSession(networkConfig, existingSession)
        lockAndMint.generateGatewayAddress()
        return@withContext lockAndMint.session
    }

    suspend fun generateSession(): LockAndMint.Session = withContext(Dispatchers.IO) {
        val signer = tokenKeyProvider.publicKey

        val existingSession = repository.findSession(signer)
        val networkConfig = NetworkConfig.DEVNET()
        lockAndMint = if (existingSession == null || !isSessionValid(existingSession)) {
            Timber.tag(TAG).d("No existing session found, building new one")
            LockAndMint.buildSession(networkConfig, signer.toPublicKey())
        } else {
            Timber.tag(TAG).d("Active session found, fetching information")
            LockAndMint.getSession(networkConfig, existingSession)
        }

        val gatewayAddress = lockAndMint.generateGatewayAddress()
        Timber.tag(TAG).d("Gateway address generated: $gatewayAddress")

        val session = lockAndMint.session
        repository.clearSessionData()
        repository.saveSession(session)
        return@withContext session
    }

    fun getPaymentDataFlow(): Flow<List<RenBTCPayment>> = state

    suspend fun startPolling(session: LockAndMint.Session) {
        if (!this::lockAndMint.isInitialized) throw IllegalStateException("LockAndMint object is not initialized")
        Timber.tag(TAG).d("Starting blockstream polling")
        val environment = environmentManager.loadEnvironment()
        val secretKey = tokenKeyProvider.secretKey
        while (isSessionValid(session)) {
            delay(SESSION_POLLING_DELAY)
            val data = repository.getPaymentData(environment, session.gatewayAddress)
            handlePaymentData(data, secretKey)
        }
    }

    private suspend fun handlePaymentData(
        data: List<RenBTCPayment>,
        secretKey: ByteArray
    ) = withContext(Dispatchers.IO) {
        data.forEach {
            lockAndMint.getDepositState(
                it.transactionHash,
                it.txIndex.toString(),
                it.amount.toString()
            )

            val txHash = lockAndMint.submitMintTransaction()
            startQueryMintPolling(txHash, secretKey)
        }
    }

    private suspend fun startQueryMintPolling(txHash: String, secretKey: ByteArray) {
        while (true) {
            val status = lockAndMint.lockAndMint(txHash)
            Timber.d("### status $status")
            if (status == "done") {
                val signature = lockAndMint.mint(Account(secretKey))
                Timber.d("### signature received $signature")
            }
            delay(1000 * 60)
        }
    }

    private fun isSessionValid(session: LockAndMint.Session): Boolean {
        val currentTime = System.currentTimeMillis()
        return currentTime < session.expiryTime
    }
}