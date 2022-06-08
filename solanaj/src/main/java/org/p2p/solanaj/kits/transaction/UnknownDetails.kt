package org.p2p.solanaj.kits.transaction

class UnknownDetails(
    signature: String,
    blockTime: Long,
    slot: Int,
    override var account: String? = null
) : TransactionDetails(signature, blockTime, slot) {
    override val type: TransactionDetailsType = TransactionDetailsType.OTHER
}
