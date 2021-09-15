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
import org.p2p.solanaj.kits.renBridge.LockAndMint
import org.p2p.solanaj.kits.renBridge.NetworkConfig
import org.p2p.solanaj.rpc.Environment

class RenBTCInteractor(
    private val repository: RenBTCRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val environmentManager: EnvironmentManager
) {

    private val state = MutableStateFlow<List<RenBTCPayment>>(emptyList())

    suspend fun getSession(): LockAndMint.Session? = withContext(Dispatchers.IO) {
        val signer = tokenKeyProvider.publicKey
        val existingSession = repository.findSession(signer) ?: return@withContext null

        val networkConfig = NetworkConfig.TESTNET()
        val lockAndMint = LockAndMint.getSession(networkConfig, existingSession)
        lockAndMint.generateGatewayAddress()
        return@withContext lockAndMint.session
    }

    suspend fun generateSession(): LockAndMint.Session = withContext(Dispatchers.IO) {
        val signer = tokenKeyProvider.publicKey

        val existingSession = repository.findSession(signer)
        val networkConfig = NetworkConfig.TESTNET()
        val lockAndMint = if (existingSession == null || !isSessionValid(existingSession)) {
            LockAndMint.buildSession(networkConfig, signer.toPublicKey())
        } else {
            LockAndMint.getSession(networkConfig, existingSession)
        }

        lockAndMint.generateGatewayAddress()

        val session = lockAndMint.session
        repository.clearSessionData()
        repository.saveSession(session)
        return@withContext session
    }

    fun getPaymentDataFlow(): Flow<List<RenBTCPayment>> = state

    suspend fun startPolling(session: LockAndMint.Session) {
        // fixme: temporary checking everything at devnet
        val network = when (val network = environmentManager.loadEnvironment()) {
            Environment.TESTNET -> "testnet"
            else -> "testnet"
//            else -> "mainnet"
        }
        while (isSessionValid(session)) {
            delay(5000L)
            val data = repository.getPaymentData(network, session.gatewayAddress)
            state.value = data
        }
    }

    private fun isSessionValid(session: LockAndMint.Session): Boolean {
        val currentTime = System.currentTimeMillis()
        return currentTime < session.expiryTime
    }
}