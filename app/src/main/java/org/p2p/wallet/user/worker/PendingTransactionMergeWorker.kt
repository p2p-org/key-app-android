package org.p2p.wallet.user.worker

import androidx.work.WorkerParameters
import androidx.work.CoroutineWorker
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import android.content.Context
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.p2p.wallet.bridge.claim.repository.EthereumBridgeLocalRepository
import org.p2p.wallet.bridge.interactor.EthereumInteractor
import org.p2p.wallet.bridge.model.BridgeBundle
import org.p2p.wallet.bridge.send.model.BridgeSendTransactionDetails
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.transaction.model.TransactionState

private const val TAG = "PendingTransactionMergeWorker"
private const val FETCH_INTERVAL_IN_MILLIS = 15_000L

class PendingTransactionMergeWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    companion object {
        fun scheduleWorker(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<PendingTransactionMergeWorker>()
                .setConstraints(constraints)
                .addTag(TAG)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(TAG, ExistingWorkPolicy.REPLACE, request)
        }
    }

    private val coroutineDispatchers: CoroutineDispatchers by inject()
    private val ethereumInteractor: EthereumInteractor by inject()
    private val transactionManager: TransactionManager by inject()
    private val ethereumBridgeLocalRepository: EthereumBridgeLocalRepository by inject()
    private val seedPhraseProvider: SeedPhraseProvider by inject()

    override suspend fun doWork(): Result = withContext(coroutineDispatchers.computation) {
        val userSeedPhrase = seedPhraseProvider.getUserSeedPhrase().seedPhrase
        if (userSeedPhrase.isNotEmpty()) {
            ethereumInteractor.setup(userSeedPhrase)
        }
        while (isActive) {
            try {
                ethereumInteractor.loadWalletTokens()
                val claimBundles = ethereumBridgeLocalRepository.getAllClaimBundles()

                claimBundles.filter(BridgeBundle::isFinalized).forEach { claimBundle ->
                    val bundleId = claimBundle.bundleId
                    transactionManager.getTransactionStateFlow(bundleId)
                        .firstOrNull()
                        ?.let { state ->
                            if (state !is TransactionState.ClaimProgress) return@let
                            val newState = TransactionState.ClaimSuccess(
                                bundleId = bundleId,
                                sourceTokenSymbol = claimBundle.resultAmount.symbol
                            )
                            transactionManager.emitTransactionState(
                                transactionId = bundleId,
                                state = newState
                            )
                        }
                }

                val sendDetails = ethereumBridgeLocalRepository.getAllSendDetails()
                sendDetails.filter(BridgeSendTransactionDetails::isFinalized).forEach { sendDetails ->
                    val bundleId = sendDetails.id
                    val newState = TransactionState.BridgeSendSuccess(
                        transactionId = bundleId,
                        sendDetails = sendDetails
                    )
                    transactionManager.emitTransactionState(
                        transactionId = bundleId,
                        state = newState
                    )
                }
                delay(FETCH_INTERVAL_IN_MILLIS)
            } catch (e: Throwable) {
                Timber.tag(TAG).e(e, "Trying to fetch bridge data")
                Result.failure()
            }
        }
        Result.success()
    }
}
