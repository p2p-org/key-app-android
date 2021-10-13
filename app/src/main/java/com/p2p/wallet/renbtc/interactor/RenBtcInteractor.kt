package com.p2p.wallet.renbtc.interactor

import com.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import com.p2p.wallet.main.model.RenBTCPayment
import com.p2p.wallet.renbtc.repository.RenBTCRepository
import com.p2p.wallet.renbtc.model.MintStatus
import com.p2p.wallet.renbtc.model.RenVMStatus
import com.p2p.wallet.renbtc.ui.main.BTC_DECIMALS
import com.p2p.wallet.utils.fromLamports
import com.p2p.wallet.utils.toPublicKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.kits.renBridge.LockAndMint
import org.p2p.solanaj.kits.renBridge.NetworkConfig
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryTxMint
import org.p2p.solanaj.rpc.Environment
import timber.log.Timber

private const val TAG = "renBTC"
private const val SESSION_POLLING_DELAY = 5000L
private const val ONE_MINUTE_DELAY = 1000L * 60L

class RenBtcInteractor(
    private val repository: RenBTCRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val environmentManager: EnvironmentManager
) {

    private lateinit var lockAndMint: LockAndMint

    private val state = MutableStateFlow<MutableList<RenVMStatus>>(mutableListOf())

    fun getRenVMStatusFlow(): Flow<List<RenVMStatus>> = state

    fun getSessionFlow(): Flow<LockAndMint.Session?> {
        val signer = tokenKeyProvider.publicKey
        return repository.findSessionFlow(signer)
    }

    suspend fun findActiveSession(): LockAndMint.Session? {
        val signer = tokenKeyProvider.publicKey
        return repository.findSession(signer)
    }

    suspend fun clearSession() {
        repository.clearSessionData()
    }

    suspend fun generateSession(): LockAndMint.Session = withContext(Dispatchers.IO) {
        val signer = tokenKeyProvider.publicKey

        val existingSession = repository.findSession(signer)
        val networkConfig = getNetworkConfig()
        lockAndMint = if (existingSession == null || !existingSession.isValid) {
            repository.clearSessionData()
            Timber.tag(TAG).d("No existing session found, building new one")
            LockAndMint.buildSession(networkConfig, signer.toPublicKey())
        } else {
            Timber.tag(TAG).d("Active session found, fetching information")
            LockAndMint.getSession(networkConfig, existingSession)
        }

        val gatewayAddress = lockAndMint.generateGatewayAddress()
        Timber.tag(TAG).d("Gateway address generated: $gatewayAddress")

        val fee = lockAndMint.estimateTransactionFee()
        Timber.tag(TAG).d("Fee calculated: $fee")

        val session = lockAndMint.session
        setStatus(RenVMStatus.Active(session.createdAt))

        repository.saveSession(session)
        return@withContext session
    }

    suspend fun startPolling(session: LockAndMint.Session) {
        if (!this::lockAndMint.isInitialized) throw IllegalStateException("LockAndMint object is not initialized")
        Timber.tag(TAG).d("Starting blockstream polling")

        val environment = environmentManager.loadEnvironment()
        val secretKey = tokenKeyProvider.secretKey
        while (session.isValid) {
            val data = repository.getPaymentData(environment, session.gatewayAddress)
            handlePaymentData(data, secretKey)
            delay(SESSION_POLLING_DELAY)
        }
    }

    private suspend fun handlePaymentData(
        data: List<RenBTCPayment>,
        secretKey: ByteArray
    ) = withContext(Dispatchers.IO) {
        Timber.tag(TAG).d("Payment data received: ${data.size}")
        setStatus(RenVMStatus.WaitingDepositConfirm)

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
            val response = lockAndMint.lockAndMint(txHash)
            handleStatus(response, secretKey)
            delay(ONE_MINUTE_DELAY)
        }
    }

    private fun handleStatus(response: ResponseQueryTxMint, secretKey: ByteArray) {
        val status = response.txStatus
        Timber.tag(TAG).d("Current mint status: $status, will check again in one minute")

        when (MintStatus.parse(status)) {
            MintStatus.CONFIRMED -> {
                setStatus(RenVMStatus.AwaitingForSignature)
            }
            MintStatus.EXECUTING -> {
                setStatus(RenVMStatus.SubmittingToRenVM)
            }
            MintStatus.DONE -> {
                if (!isValidStatus()) return

                setStatus(RenVMStatus.Minting)
                val signature = lockAndMint.mint(Account(secretKey))
                Timber.tag(TAG).d("Mint signature received: $signature")
                val amount = response.valueOut.amount.toBigInteger().fromLamports(BTC_DECIMALS)

                setStatus(RenVMStatus.SuccessfullyMinted(amount))
            }
        }
    }

    private fun isValidStatus(): Boolean {
        val latestStatus = state.value.lastOrNull()
        if (latestStatus is RenVMStatus.Minting || latestStatus is RenVMStatus.SuccessfullyMinted) return false

        return true
    }

    private fun setStatus(status: RenVMStatus) {
        state.value = state.value.toMutableList().also { statuses ->
            val latestStatus = statuses.lastOrNull()
            when {
                latestStatus is RenVMStatus.SuccessfullyMinted ->
                    if (status !is RenVMStatus.Minting) statuses.add(status)
                latestStatus != status ->
                    statuses.add(status)
            }
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