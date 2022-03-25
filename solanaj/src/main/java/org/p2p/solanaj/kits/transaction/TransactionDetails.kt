package org.p2p.solanaj.kits.transaction

import java.util.concurrent.TimeUnit

sealed class TransactionDetails(
    val signature: String,
    val blockTimeSeconds: Long,
    val slot: Int
) {
    abstract val type: TransactionDetailsType

    /*
    * Since blocktime is time of when the transaction was processed in SECONDS
    * we are converting it into milliseconds
    * */
    val blockTimeMillis: Long
        get() = TimeUnit.SECONDS.toMillis(blockTimeSeconds)
}
