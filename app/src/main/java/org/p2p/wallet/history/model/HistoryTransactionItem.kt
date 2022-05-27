package org.p2p.wallet.history.model

import org.p2p.solanaj.kits.transaction.TransactionDetailsType
import org.p2p.solanaj.model.types.ConfirmationStatus
import java.util.concurrent.TimeUnit

sealed class HistoryTransactionItem(
    val signature: String,
    val blockTimeSeconds: Long,
    val slot: Int,
    val error: List<Any>? = null,
    val confirmationStatus: ConfirmationStatus? = null
) {
    abstract val type: TransactionDetailsType

    val blockTimeMillis: Long
        get() = TimeUnit.SECONDS.toMillis(blockTimeSeconds)
}
