package org.p2p.wallet.feerelayer.interactor

import kotlinx.coroutines.withContext
import org.p2p.solanaj.core.PreparedTransaction
import org.p2p.solanaj.utils.crypto.toBase64Instance
import org.p2p.wallet.feerelayer.model.FeeRelayerStatistics
import org.p2p.wallet.feerelayer.repository.FeeRelayerRepository
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.rpc.interactor.TransactionInteractor
import org.p2p.wallet.utils.retryRequest

class FeeRelayerViaLinkInteractor(
    private val feeRelayerRepository: FeeRelayerRepository,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor,
    private val transactionInteractor: TransactionInteractor,
    private val dispatchers: CoroutineDispatchers
) {

    suspend fun load() = withContext(dispatchers.io) {
        feeRelayerAccountInteractor.getRelayInfo()
        feeRelayerAccountInteractor.getUserRelayAccount(useCache = false)
        feeRelayerAccountInteractor.getFreeTransactionFeeLimit(useCache = false)
    }

    suspend fun signAndSendTransaction(
        preparedTransaction: PreparedTransaction,
        statistics: FeeRelayerStatistics,
        isRetryEnabled: Boolean = true,
        isSimulation: Boolean
    ): String {
        // resign transaction
        val transaction = preparedTransaction.transaction

        // sign transaction by user
        transaction.sign(preparedTransaction.signers)

        // adding fee payer signature
        val serializedTransaction = transaction.serialize().toBase64Instance()
        val signedTransaction = feeRelayerRepository.signTransaction(serializedTransaction, statistics)

        /*
         * Retrying 3 times to avoid some errors
         * For example: fee relayer balance is not updated yet and request will fail with insufficient balance error
         * */
        return if (isRetryEnabled) {
            retryRequest { transactionInteractor.sendTransaction(signedTransaction.transaction, isSimulation) }
        } else {
            transactionInteractor.sendTransaction(signedTransaction.transaction, isSimulation)
        }
    }
}
