package org.p2p.solanaj.kits.transaction

abstract class TransactionDetails(
    open val signature: String,
    private val blockTime: Long,
    open val slot: Int
) {
    abstract val type: TransactionDetailsType?
    abstract val info: Any?
    open val data: String?
        get() = null

    fun getBlockTimeInMillis(): Long {
        /*
         * Since blocktime is time of when the transaction was processed in SECONDS
         * we are converting it into milliseconds
         * */
        return blockTime * 1000
    }
}
