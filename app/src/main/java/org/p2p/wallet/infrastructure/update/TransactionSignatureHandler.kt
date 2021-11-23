package org.p2p.wallet.infrastructure.update

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.p2p.wallet.transaction.interactor.TransactionInteractor
import org.p2p.wallet.updates.UpdateHandler
import org.p2p.wallet.updates.UpdateType
import timber.log.Timber

class TransactionSignatureHandler(
    private val transactionInteractor: TransactionInteractor
) : UpdateHandler {

    override suspend fun initialize() {
        // Nothing to initialize for transaction signatures
    }

    override suspend fun onUpdate(type: UpdateType, data: Any) {
        if (type != UpdateType.SIGNATURE_RECEIVED) {
            return
        }

        coroutineScope {
            launch {
                try {
                    val signature = data as? String
                    transactionInteractor.onSignatureReceived(signature.orEmpty())
                    Timber.tag("SOCKET").d("Transaction update received, passing data further")
                } catch (e: Throwable) {
                    Timber.tag("SOCKET").e(e, "Failed to load operation received by update. Data = $data")
                }
            }
        }
    }
}