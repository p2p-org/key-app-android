package org.p2p.wallet.feerelayer.interactor

import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.p2p.solanaj.core.PreparedTransaction
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
        isSimulation: Boolean = false
    ): String {
        // resign transaction
        val transaction = preparedTransaction.transaction

        // sign transaction by user
        transaction.sign(preparedTransaction.signers)

        if (isSimulation) {
            // TODO: REMOVE FAKE TRANSACTION AFTER FEE RELAYER IS FIXED
            delay(1000L)
            return UUID.randomUUID().toString()
        }

        // adding fee payer signature
//        val signature = feeRelayerRepository.signTransaction(transaction, statistics)
//        val feePayer = feeRelayerAccountInteractor.getRelayInfo().feePayerAddress
//        transaction.addSignature(
//            Signature(
//                publicKey = feePayer,
//                signature = signature.signature.base58Value
//            )
//        )

        /*
         * Retrying 3 times to avoid some errors
         * For example: fee relayer balance is not updated yet and request will fail with insufficient balance error
         * */
        return if (isRetryEnabled) {
            retryRequest { transactionInteractor.serializeAndSend(transaction, isSimulation) }
        } else {
            transactionInteractor.serializeAndSend(transaction, isSimulation)
        }
    }
}
