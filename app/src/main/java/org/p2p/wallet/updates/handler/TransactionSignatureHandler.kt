package org.p2p.wallet.updates.handler

import com.google.gson.JsonObject
import timber.log.Timber
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.p2p.wallet.transaction.interactor.TransactionStatusInteractor
import org.p2p.wallet.updates.SocketSubscriptionUpdateType
import org.p2p.wallet.updates.SubscriptionUpdateHandler

private const val TAG = "TransactionSignatureHandler"
class TransactionSignatureHandler(
    private val transactionStatusInteractor: TransactionStatusInteractor
) : SubscriptionUpdateHandler {

    override suspend fun initialize() = Unit

    override suspend fun onUpdate(type: SocketSubscriptionUpdateType, data: JsonObject) {
        if (type != SocketSubscriptionUpdateType.TX_SIGNATURE_UPDATED) {
            return
        }
        coroutineScope {
            launch {
                try {
                    val signature = data.toString()
                    transactionStatusInteractor.onSignatureReceived(signature)
                    Timber.tag(TAG).d("Transaction update received, passing data further")
                } catch (e: Throwable) {
                    Timber.tag(TAG).e(e, "Failed to load operation received by update. Data = $data")
                }
            }
        }
    }
}
