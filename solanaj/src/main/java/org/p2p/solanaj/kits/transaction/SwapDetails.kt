package org.p2p.solanaj.kits.transaction

class SwapDetails(
    signature: String,
    blockTime: Long,
    slot: Int,
    val fee: Long,
    val source: String?,
    val destination: String?,
    val amountA: String,
    val amountB: String,
    val mintA: String?,
    val mintB: String?,
    val alternateSource: String?,
    val alternateDestination: String?,
) : TransactionDetails(signature, blockTime, slot) {
    override val type: TransactionDetailsType = TransactionDetailsType.SWAP
}
