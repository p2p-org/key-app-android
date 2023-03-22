package org.p2p.wallet.feerelayer.interactor

import kotlinx.coroutines.withContext
import org.p2p.solanaj.core.PreparedTransaction
import org.p2p.wallet.feerelayer.model.FeeRelayerStatistics
import org.p2p.wallet.feerelayer.repository.FeeRelayerRepository
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.utils.retryRequest

class FeeRelayerViaLinkInteractor(
    private val feeRelayerRepository: FeeRelayerRepository,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor,
    private val dispatchers: CoroutineDispatchers
) {

    suspend fun load() = withContext(dispatchers.io) {
        feeRelayerAccountInteractor.getRelayInfo()
        feeRelayerAccountInteractor.getUserRelayAccount(useCache = false)
        feeRelayerAccountInteractor.getFreeTransactionFeeLimit(useCache = false)
    }

    suspend fun relayTransaction(
        preparedTransaction: PreparedTransaction,
        statistics: FeeRelayerStatistics
    ): List<String> {
        // resign transaction
        val transaction = preparedTransaction.transaction
        transaction.sign(preparedTransaction.signers)

        /*
        * Retrying 3 times to avoid some errors
        * For example: fee relayer balance is not updated yet and request will fail with insufficient balance error
        * */
        return retryRequest {
            feeRelayerRepository.relayTransaction(transaction, statistics)
        }
    }
}
